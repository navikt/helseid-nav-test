package no.nav.helseidnavtest.oppslag.graphql

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.graphql.GraphQLExtensions.oversett
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.ResponseError
import org.springframework.graphql.client.*
import org.springframework.graphql.client.SyncGraphQlClientInterceptor.Chain
import org.springframework.http.HttpStatus.*
import org.springframework.web.client.RestClient
import java.net.URI

abstract class AbstractGraphQLAdapter(client : RestClient, cfg : AbstractRestConfig, protected val errorHandler: GraphQLErrorHandler = object : GraphQLErrorHandler {}) : AbstractRestClientAdapter(client, cfg) {

    protected inline fun <reified T> query(graphQL : GraphQlClient, query : Pair<String, String>, vars : Map<String, String>) =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .executeSync()
                .field(query.second)
                .toEntity(T::class.java)
        }.getOrElse {
            log.warn("Feil ved oppslag av {}", T::class.java.simpleName, it)
            errorHandler.handle(cfg.baseUri, it)
        }
}


interface GraphQLErrorHandler {
    fun handle(uri: URI, e : Throwable) : Nothing =
        when (e) {
            is FieldAccessException -> throw e.oversett(uri)
            is GraphQlTransportException -> throw RecoverableException(INTERNAL_SERVER_ERROR, e.message ?: "Transport feil", uri, e)
            else -> throw IrrecoverableException(INTERNAL_SERVER_ERROR, "Ikke håndtert", e.message, uri, e)
        }
}
class LoggingGraphQLInterceptor : SyncGraphQlClientInterceptor {

    private val log = getLogger(LoggingGraphQLInterceptor::class.java)

    override fun intercept(req: ClientGraphQlRequest, chain: Chain) =
        chain.next(req).also {
           log.trace("Eksekverer {} med variabler {}", req.document, req.variables)
        }
}
object GraphQLExtensions {

    private val log = getLogger(javaClass)

    fun FieldAccessException.oversett(uri: URI) = response.errors.oversett(message,uri)

    private fun List<ResponseError>.oversett(message: String?, uri: URI) = oversett(firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil", uri)
        .also {
            log.warn("GraphQL oppslag returnerte $size feil, oversatte $message til ${it.javaClass.simpleName}", this)
        }

    private fun oversett(kode : String?, msg : String, uri: URI) =
        when (kode) {
            "unauthorized" -> IrrecoverableException(UNAUTHORIZED, kode, msg, uri)
            "unauthenticated" -> IrrecoverableException(FORBIDDEN, kode, msg, uri)
            "bad_request" -> IrrecoverableException(BAD_REQUEST, kode, msg, uri)
            "not_found" -> NotFoundException(detail = msg, uri = uri)
            else -> IrrecoverableException(INTERNAL_SERVER_ERROR, kode ?: "Ukjent", msg, uri)
        }
}

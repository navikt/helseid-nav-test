package no.nav.helseidnavtest.oppslag.graphql

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.graphql.GraphQLExtensions.oversett
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.client.*
import org.springframework.graphql.client.SyncGraphQlClientInterceptor.Chain
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.client.RestClient
import java.net.URI

abstract class AbstractGraphQLAdapter(client : RestClient, cfg : AbstractRestConfig, protected val handler: GraphQLErrorHandler = object : GraphQLErrorHandler {}) : AbstractRestClientAdapter(client, cfg) {

    protected inline fun <reified T : Any> query(graphQL : GraphQlClient, query : Pair<String, String>, vars : Map<String, String>) =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .executeSync()
                .field(query.second)
                .entityType(T::class)
               // .toEntity(T::class.java)
        }.getOrElse {
            log.warn("Feil ved oppslag av {}", T::class.java.simpleName, it)
            handler.handle(cfg.baseUri, it)
        }
    inline fun <reified T: Any> ClientResponseField.entityType(kClass: T) : Any =
        when (kClass) {
            List::class -> toEntityList(T::class.java)
            else -> toEntity(T::class.java)
        }
}



interface GraphQLErrorHandler {
    fun handle(uri: URI, e : Throwable) : Nothing =
        when (e) {
            is FieldAccessException -> throw e.oversett(uri)
            is GraphQlTransportException -> throw RecoverableException(INTERNAL_SERVER_ERROR, e.message ?: "Transport feil", uri, e)
            is IrrecoverableException -> throw e
            else -> throw IrrecoverableException(INTERNAL_SERVER_ERROR, "Ikke håndtert", e.message, uri, e)
        }
}
class LoggingGraphQLInterceptor : SyncGraphQlClientInterceptor {

    private val log = getLogger(LoggingGraphQLInterceptor::class.java)

    override fun intercept(request: ClientGraphQlRequest, chain: Chain) =
        chain.next(request).also {
           log.trace("Eksekverer {} ", request.document)
        }
}


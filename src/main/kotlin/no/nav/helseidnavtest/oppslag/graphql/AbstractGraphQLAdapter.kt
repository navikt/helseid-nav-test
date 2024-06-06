package no.nav.helseidnavtest.oppslag.graphql

import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
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

    protected inline fun <reified T> query(graphQL : GraphQlClient, query : Pair<String, String>, vars : Map<String, String>) =
        runCatching {
           log.info("{} Eksekverer {} med {}",graphQL, T::class.java.simpleName, vars)
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .onErrorMap {
                    when(it) {
                        is FieldAccessException -> it.oversett(cfg.baseUri)
                        is GraphQlTransportException -> RecoverableException(INTERNAL_SERVER_ERROR, it.message ?: "Transport feil", cfg.baseUri, it
                        )
                        else ->  it
                    }
                }
                .block().also {
                    log.trace(CONFIDENTIAL,"Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse {
            log.warn("Feil ved oppslag av {}", T::class.java.simpleName, it)
            handler.handle(cfg.baseUri, it)
        }
}

/* Denne kalles når retry har gitt opp */
interface GraphQLErrorHandler {
    fun handle(uri: URI, e : Throwable) : Nothing = when (e) {
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


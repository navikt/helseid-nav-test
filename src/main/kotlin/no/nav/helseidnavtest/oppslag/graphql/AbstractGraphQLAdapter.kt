package no.nav.helseidnavtest.oppslag.graphql

import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.*
import no.nav.helseidnavtest.error.RecoverableGraphQLException
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.rest.AbstractWebClientAdapter
import no.nav.helseidnavtest.oppslag.graphql.GraphQLExtensions.oversett
import org.slf4j.LoggerFactory.getLogger
import org.springframework.graphql.client.*
import org.springframework.graphql.client.GraphQlClientInterceptor.Chain
import org.springframework.http.HttpStatus.*
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

abstract class AbstractGraphQLAdapter(client : WebClient, cfg : AbstractRestConfig, val handler : GraphQLErrorHandler) : AbstractWebClientAdapter(client, cfg) {


    protected inline fun <reified T> query(graphQL : GraphQlClient, query : Pair<String, String>, vars : Map<String, String>) =
        runCatching {
           log.info("Eksekverer {} med {}", T::class.java.simpleName, vars)
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .onErrorMap {
                    when(it) {
                        is FieldAccessException -> it.oversett(cfg.baseUri)
                        is GraphQlTransportException -> BadGraphQLException(it.message ?: "Transport feil", cfg.baseUri,it)
                        else ->  it
                    }
                }
               // .retryWhen(retrySpec(log, "/graphql") { it is RecoverableGraphQLException })
                .block().also {
                    log.trace(CONFIDENTIAL,"Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse {
            log.warn("Feil ved oppslag av {}", T::class.java.simpleName, it)
            handler.handle(cfg.baseUri, it)
        }
}

 class LoggingGraphQLInterceptor : GraphQlClientInterceptor {

    private val log = getLogger(LoggingGraphQLInterceptor::class.java)

     override fun intercept(request : ClientGraphQlRequest, chain : Chain) =
         chain.next(request).also {
             log.trace("Eksekverer {} ", request.document)
         }
}

/* Denne kalles når retry har gitt opp */
interface GraphQLErrorHandler {
    fun handle(uri: URI, e : Throwable) : Nothing = when (e) {
        is IrrecoverableGraphQLException -> throw e
        is RecoverableGraphQLException -> throw e
        else -> throw IrrecoverableException(e.message, uri,e)
    }
}
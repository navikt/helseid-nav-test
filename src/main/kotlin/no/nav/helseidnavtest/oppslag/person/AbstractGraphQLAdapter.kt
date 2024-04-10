package no.nav.helseidnavtest.oppslag.person

import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.person.GraphQLExtensions.oversett
import org.springframework.graphql.client.FieldAccessException
import org.springframework.graphql.client.GraphQlClient
import org.springframework.graphql.client.GraphQlTransportException
import org.springframework.http.HttpStatus.*
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(client : WebClient, cfg : AbstractRestConfig, val handler : GraphQLErrorHandler) : AbstractWebClientAdapter(client, cfg) {


    protected inline fun <reified T> query(graphQL : GraphQlClient, query : Pair<String, String>, vars : Map<String, String>) =
        runCatching {
            graphQL
                .documentName(query.first)
                .variables(vars)
                .retrieve(query.second)
                .toEntity(T::class.java)
                .onErrorMap {
                    when(it) {
                        is FieldAccessException -> it.oversett()
                        is GraphQlTransportException -> IrrecoverableGraphQLException.BadGraphQLException(BAD_REQUEST, it.message ?: "Transport feil", it)
                        else ->  it
                    }
                }
               // .retryWhen(retrySpec(log, "/graphql") { it is RecoverableGraphQLException })
                .contextCapture()
                .block().also {
                    log.trace(CONFIDENTIAL,"Slo opp {} {}", T::class.java.simpleName, it)
                }
        }.getOrElse { t ->
            handler.handle(t)
        }
}


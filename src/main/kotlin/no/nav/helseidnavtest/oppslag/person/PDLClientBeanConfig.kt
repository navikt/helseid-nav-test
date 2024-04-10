package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.error.IntegrationException
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.PDL_SYSTEM
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.behandlingFilterFunction
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.temaFilterFunction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.ClientGraphQlRequest
import org.springframework.graphql.client.GraphQlClientInterceptor
import org.springframework.graphql.client.GraphQlClientInterceptor.Chain
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.WebClient

@Configuration(proxyBeanMethods = false)
class PDLClientBeanConfig {

    @Bean
    fun graphQLErrorHandler() = object : GraphQLErrorHandler {}

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun pdlSystemWebClient(b: WebClient.Builder, cfg: PDLConfig) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .build()

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun graphQLSystemWebClient(@Qualifier(PDL_SYSTEM) client: WebClient) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()


    @Bean
    fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}

private class LoggingGraphQLInterceptor : GraphQlClientInterceptor {

    private val log = LoggerFactory.getLogger(LoggingGraphQLInterceptor::class.java)

    override fun intercept(request : ClientGraphQlRequest, chain : Chain) = chain.next(request).also {
        log.trace("Eksekverer {} ", request.document)
    }
}

/* Denne kalles når retry har gitt opp */
interface GraphQLErrorHandler {
    fun handle(e : Throwable) : Nothing = when (e) {
        is IntegrationException -> throw e
        else -> throw IrrecoverableException(e.message, cause = e)
    }
}
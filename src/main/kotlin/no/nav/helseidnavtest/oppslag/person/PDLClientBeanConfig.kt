package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import no.nav.helseidnavtest.oppslag.graphql.LoggingGraphQLInterceptor
import no.nav.helseidnavtest.oppslag.rest.AbstractWebClientAdapter.Companion.behandlingFilterFunction
import no.nav.helseidnavtest.oppslag.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.helseidnavtest.oppslag.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class PDLClientBeanConfig

    @Bean
    fun graphQLErrorHandler() = object : GraphQLErrorHandler {}

@Bean
@Qualifier(PDL)
fun pdlWebClient(b: Builder, cfg: PDLConfig, oauthFilterFunction: ServletOAuth2AuthorizedClientExchangeFilterFunction) =
    b.apply(oauthFilterFunction.oauth2Configuration())
        .baseUrl("${cfg.baseUri}")
        .filter(correlatingFilterFunction("helseidnavtest"))
        .filter(temaFilterFunction())
        .filter(behandlingFilterFunction())
        .build()

    @Bean
    fun oauthFilterFunction(authorizedClientManager: OAuth2AuthorizedClientManager) : ServletOAuth2AuthorizedClientExchangeFilterFunction {
       val ff =  ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        ff.setDefaultClientRegistrationId(PDL)
        return ff
    }

    @Bean
    @Qualifier(PDL)
    fun graphQLWebClient(@Qualifier(PDL) client: WebClient) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()
    @Bean
    fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}





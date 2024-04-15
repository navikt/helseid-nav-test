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
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
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
        b.baseUrl("${cfg.baseUri}")
            .apply(oauthFilterFunction.oauth2Configuration())
            .filter(correlatingFilterFunction("helseidnavtest"))
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .build()

    @Bean
    fun oauthFilterFunction(authorizedClientManager: OAuth2AuthorizedClientManager) : ServletOAuth2AuthorizedClientExchangeFilterFunction {
       val s =  ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        s.setDefaultClientRegistrationId(PDL)
        return s
    }

    @Bean
    @Qualifier(PDL)
    fun graphQLWebClient(@Qualifier(PDL) client: WebClient) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()
    @Bean
    fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}





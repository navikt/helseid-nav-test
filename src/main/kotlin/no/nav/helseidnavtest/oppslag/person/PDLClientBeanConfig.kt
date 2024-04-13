package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.error.IntegrationException
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import no.nav.helseidnavtest.oppslag.graphql.LoggingGraphQLInterceptor
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.behandlingFilterFunction
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.helseidnavtest.oppslag.person.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.*


@Configuration(proxyBeanMethods = false)
class PDLClientBeanConfig {

    @Bean
    fun graphQLErrorHandler() = object : GraphQLErrorHandler {}

    @Bean
    @Qualifier(PDL)
    fun pdlWebClient(b: Builder, cfg: PDLConfig,@Qualifier(PDL) oauth: ServerOAuth2AuthorizedClientExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(correlatingFilterFunction("helseidnavtest"))
            .filter(oauth)
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .build()

    @Bean
    @Qualifier(PDL)
    fun oauthFilterFunction(clientRegistrations: ReactiveClientRegistrationRepository, authorizedClientService: ReactiveOAuth2AuthorizedClientService) =
        ServerOAuth2AuthorizedClientExchangeFilterFunction(AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, authorizedClientService)).setDefaultClientRegistrationId(PDL)
    }

    @Bean
    @Qualifier(PDL)
    fun graphQLWebClient(@Qualifier(PDL) client: WebClient) =
        HttpGraphQlClient.builder(client)
            .interceptor(LoggingGraphQLInterceptor())
            .build()

    @Bean
    fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}


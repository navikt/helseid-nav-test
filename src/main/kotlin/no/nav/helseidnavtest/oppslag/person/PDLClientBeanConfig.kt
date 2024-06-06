package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.HELSE
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.behandlingRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.consumerRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.temaRequestInterceptor
import no.nav.helseidnavtest.oppslag.graphql.LoggingGraphQLInterceptor
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpSyncGraphQlClient
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.client.RestClient


@Configuration
class PDLClientBeanConfig {
    protected val log = getLogger(PDLController::class.java)

    @Bean
    @Qualifier(PDL)
    fun pdlRestClient(b: RestClient.Builder, cfg: PDLConfig, @Qualifier(PDL) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) : RestClient =
        b.requestInterceptors {
           it.addAll(
           listOf(clientCredentialsRequestInterceptor,temaRequestInterceptor(HELSE),consumerRequestInterceptor(),behandlingRequestInterceptor()))
       }

      /*  b.requestInterceptor(clientCredentialsRequestInterceptor)
            .requestInterceptor(temaRequestInterceptor(HELSE))
            .requestInterceptor(consumerRequestInterceptor())
            .requestInterceptor(behandlingRequestInterceptor())*/
            .build()

    @Bean
    @Qualifier(PDL)
    fun syncGraphQLClient(@Qualifier(PDL) client: RestClient,cfg: PDLConfig) : HttpSyncGraphQlClient =
        HttpSyncGraphQlClient.builder(client)
            .url(cfg.baseUri)
           .interceptor(LoggingGraphQLInterceptor())
            .build()
    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(repo: ClientRegistrationRepository, service: OAuth2AuthorizedClientService) = AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service)

    @Bean
    @Qualifier(PDL)
    fun pdlClientCredentialsRequestInterceptor(clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) = ClientHttpRequestInterceptor { req, body, execution ->
        clientManager.authorize(
            withClientRegistrationId(PDL)
                .principal("anonymous")
                .build()
        )?.let {
            req.headers.setBearerAuth(it.accessToken.tokenValue)
        }
        execution.execute(req, body)
    }

    @Bean
    fun pdlHealthIndicator(a: PDLRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}






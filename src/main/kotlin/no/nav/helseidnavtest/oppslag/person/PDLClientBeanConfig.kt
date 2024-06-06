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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient


@Configuration
class PDLClientBeanConfig {
    protected val log = getLogger(PDLController::class.java)

    @Bean
    @Qualifier(PDL)
    fun pdlRestClient(b: RestClient.Builder, cfg: PDLConfig, @Qualifier(PDL) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) : RestClient =
       b.requestInterceptor(clientCredentialsRequestInterceptor)
           .requestInterceptor(temaRequestInterceptor(HELSE))
           .requestInterceptor(consumerRequestInterceptor())
           .requestInterceptor(behandlingRequestInterceptor()).build().also {
            log.info("Opprettet PDL REST klient $it")
        }

    @Bean
    @Qualifier(PDL)
    fun syncGraphQLClient(@Qualifier(PDL) client: RestClient,cfg: PDLConfig) : HttpSyncGraphQlClient =
        HttpSyncGraphQlClient.builder(client)
            .url(cfg.baseUri)
           .interceptor(LoggingGraphQLInterceptor())
            .build().also {
                log.info("Opprettet PDL GraphQL klient $it med $client")
            }

    @Bean
    @Qualifier(PDL)
    fun pdlClientCredentialsRequestInterceptor(clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) = ClientHttpRequestInterceptor { req, body, execution ->
       log.info("Setter PDL token med client manager $clientManager og request $req")
        clientManager.authorize(
            withClientRegistrationId(PDL)
                .principal("anonymous")
                .build()
        )?.let {
            log.info("Fant token for PDL")
            req.headers.setBearerAuth(it.accessToken.tokenValue)
        } ?: log.warn("Fant ikke token for PDL")

        execution.execute(req, body)
    }

    @Bean
    fun pdlHealthIndicator(a: PDLRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}






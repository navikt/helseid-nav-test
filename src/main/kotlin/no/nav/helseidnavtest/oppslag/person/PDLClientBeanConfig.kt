package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.HELSE
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.behandlingRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.consumerRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.correlatingRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.temaRequestInterceptor
import no.nav.helseidnavtest.oppslag.TokenExchangingRequestInterceptor
import no.nav.helseidnavtest.oppslag.graphql.LoggingGraphQLInterceptor
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpSyncGraphQlClient
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder


@Configuration(proxyBeanMethods = false)
class PDLClientBeanConfig {

    @Bean
    @Qualifier(PDL)
    fun pdlRestClient(b: Builder, @Qualifier(PDL) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) =
        b.requestInterceptors {
            it.addAll(
                listOf(
                    clientCredentialsRequestInterceptor,
                    temaRequestInterceptor(HELSE),
                    consumerRequestInterceptor(),
                    behandlingRequestInterceptor()
                )
            )
        }.build()

    @Bean
    @Qualifier(PDL)
    fun syncPdlGraphQLClient(@Qualifier(PDL) client: RestClient, cfg: PDLConfig) =
        HttpSyncGraphQlClient.builder(client)
            .url(cfg.baseUri)
            .interceptor(LoggingGraphQLInterceptor())
            .build()

    @Bean
    @Qualifier(PDL)
    fun pdlClientCredentialsRequestInterceptor(clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) =
        TokenExchangingRequestInterceptor(
            clientManager,
            defaultShortName = PDL
        )

    @Bean
    fun pdlHealthIndicator(a: PDLRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Bean
    fun restClientCustomizer() = RestClientCustomizer {
        it.requestInterceptor(correlatingRequestInterceptor(HELSE))
        it.requestFactory(HttpComponentsClientHttpRequestFactory())
    }
}




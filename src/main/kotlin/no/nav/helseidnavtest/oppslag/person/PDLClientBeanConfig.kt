package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.HELSE
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.behandlingRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.consumerRequestInterceptor
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.temaRequestInterceptor
import no.nav.helseidnavtest.oppslag.graphql.LoggingGraphQLInterceptor
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.graphql.client.HttpSyncGraphQlClient
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient


@Configuration
class PDLClientBeanConfig {

    @Bean
    @Qualifier(PDL)
    fun pdlRestClient(b: RestClient.Builder, cfg: PDLConfig, @Qualifier(PDL) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) : RestClient =
        b.requestInterceptors {
            listOf(temaRequestInterceptor(HELSE),
                clientCredentialsRequestInterceptor,
                consumerRequestInterceptor(),
                behandlingRequestInterceptor())
        }.build()

    @Bean
    @Qualifier(PDL)
    fun syncGraphQLClient(@Qualifier(PDL) client: RestClient,cfg: PDLConfig) : HttpSyncGraphQlClient =
        HttpSyncGraphQlClient.builder(client)
            .url(cfg.baseUri)
            .interceptor(LoggingGraphQLInterceptor())
            .build()

    @Bean
    @Qualifier(PDL)
    fun pdlClientCredentialsRequestInterceptor(clientManager: OAuth2AuthorizedClientManager) = ClientHttpRequestInterceptor { request, body, execution ->
        clientManager.authorize(
            withClientRegistrationId(PDL)
                .principal("anonymous")
                .build()
        )?.let {
            request.headers.setBearerAuth(it.accessToken.tokenValue)
        }
        execution.execute(request, body)
    }

    @Bean
    fun pdlHealthIndicator(a: PDLRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}






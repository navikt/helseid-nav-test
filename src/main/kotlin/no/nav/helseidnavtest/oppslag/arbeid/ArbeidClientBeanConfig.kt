package no.nav.helseidnavtest.oppslag.arbeid

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.TokenExchangingRequestInterceptor
import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient.Builder

@Configuration
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun restClientArbeidsforhold(builder: Builder, cfg: ArbeidConfig, @Qualifier(ARBEID) filter: ClientHttpRequestInterceptor) =
        builder
            .baseUrl("${cfg.baseUri}")
            .requestInterceptor(filter)
            .build()

    @Bean
    @ConditionalOnProperty("$ARBEID.enabled", havingValue = "true", matchIfMissing = true)
    fun arbeidsforholdHealthIndicator(a: ArbeidRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Bean
    @Qualifier(ARBEID)
    fun arbeidClientCredentialsRequestInterceptor(clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) = TokenExchangingRequestInterceptor("Bearer",ARBEID, clientManager)
}
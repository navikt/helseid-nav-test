package no.nav.helseidnavtest.oppslag.arbeid

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.helseidnavtest.security.ClientCredentialsRequestInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient.Builder

@Configuration
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun restClientArbeidsforhold(builder: Builder, cfg: ArbeidConfig, filter: ClientCredentialsRequestInterceptor) =
        builder
            .baseUrl("${cfg.baseUri}")
            .requestInterceptor(filter)
            .build()

    @Bean
    @ConditionalOnProperty("$ARBEID.enabled", havingValue = "true", matchIfMissing = true)
    fun arbeidsforholdHealthIndicator(a: ArbeidRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}
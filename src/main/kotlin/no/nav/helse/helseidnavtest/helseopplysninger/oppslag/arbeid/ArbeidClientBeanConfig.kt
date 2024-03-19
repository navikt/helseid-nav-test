package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.helse.helseidnavtest.helseopplysninger.health.AbstractPingableHealthIndicator
import no.nav.helse.helseidnavtest.helseopplysninger.security.ClientCredentialsTokenInterceptor
import org.springframework.web.client.RestClient.Builder

@Configuration
class ArbeidClientBeanConfig(private val filter: ClientCredentialsTokenInterceptor) {

    @Bean
    @Qualifier(ARBEID)
    fun restClientArbeidsforhold(builder: Builder, cfg: ArbeidConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .requestInterceptor(filter)
            .build()

    @Bean
    @ConditionalOnProperty("$ARBEID.enabled", havingValue = "true", matchIfMissing = true)
    fun arbeidsforholdHealthIndicator(a: ArbeidRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}
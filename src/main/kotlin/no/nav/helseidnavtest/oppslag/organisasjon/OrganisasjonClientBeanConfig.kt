package no.nav.helseidnavtest.oppslag.organisasjon

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.helse.helseidnavtest.helseopplysninger.health.AbstractPingableHealthIndicator
import org.springframework.web.client.RestClient

@Configuration
internal class OrganisasjonClientBeanConfig {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonRestClient(builder: RestClient.Builder, cfg: OrganisasjonConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$ORGANISASJON.enabled", havingValue = "true", matchIfMissing = true)
    fun organisasjonHealthIndicator(a: OrganisasjonRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}
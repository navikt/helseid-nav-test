package no.nav.helseidnavtest.oppslag.organisasjon

import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseidnavtest.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.OrganisasjonConfig.Companion.ORGANISASJON
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
    @ConditionalOnProperty("$ORGANISASJON.enabled", havingValue = "true")
    fun organisasjonHealthIndicator(a: OrganisasjonRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}
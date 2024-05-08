package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient.Builder

@Configuration
class DialogmeldingClientBeanConfig {

    @Bean
    @Qualifier(DIALOGMELDING)
    fun restClientDialogmelding(builder: Builder, cfg: DialogmeldingConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$DIALOGMELDING.enabled", havingValue = "true", matchIfMissing = true)
    fun dialogmeldingdHealthIndicator(a: DialogmeldingRestAdapter) = object : AbstractPingableHealthIndicator(a) {}

}
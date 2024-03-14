package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.web.client.RestClient

@Configuration
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun webClientArbeidsforhold(builder: RestClient.Builder, cfg: ArbeidConfig,/* tokenX: TokenXFilterFunction, ctx: AuthContext*/) =
        builder
            .baseUrl("${cfg.baseUri}")
      //      .filter(generellFilterFunction(NAV_PERSON_IDENT) { ctx.getSubject() ?: "NO SUBJECT" })
      //      .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$ARBEID.enabled", havingValue = "true")
    fun arbeidsforholdHealthIndicator(a: ArbeidRestClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}
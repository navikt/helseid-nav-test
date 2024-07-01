package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.DPopEnabledTokenExchangingRequestInterceptor
import no.nav.helseidnavtest.security.DPoPProofGenerator
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient.*


@Configuration(proxyBeanMethods = true)
class EDI20BeanConfig {

    private val log = getLogger(EDI20BeanConfig::class.java)


    @Bean
    @Qualifier(EDI20)
    fun edi20RestClient(b: Builder, cfg: EDI20Config,@Qualifier(EDI20) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) =
        b.baseUrl("${cfg.baseUri}")
        .requestInterceptors {
           it.add(clientCredentialsRequestInterceptor)
       }.build().also {
              log.info("Created EDI20RestClient for $clientCredentialsRequestInterceptor")
            }

    @Bean
    @Qualifier(EDI20)
    fun edi20ClientCredentialsRequestInterceptor(proofGenerator: DPoPProofGenerator, clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) = DPopEnabledTokenExchangingRequestInterceptor (
        proofGenerator, "$EDI20-1", clientManager)
}




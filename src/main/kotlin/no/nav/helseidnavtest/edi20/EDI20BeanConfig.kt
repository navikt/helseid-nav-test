package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.TokenExchangingRequestInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient.*


@Configuration(proxyBeanMethods = false)
class EDI20BeanConfig {

    @Bean
    @Qualifier(EDI20)
    fun edi20RestClient(b: Builder, @Qualifier(EDI20) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) =
        b.requestInterceptors {
           it.add(clientCredentialsRequestInterceptor)
       }.build()

    @Bean
    @Qualifier(EDI20)
    fun edi20ClientCredentialsRequestInterceptor(clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) = TokenExchangingRequestInterceptor(EDI20, clientManager)
}




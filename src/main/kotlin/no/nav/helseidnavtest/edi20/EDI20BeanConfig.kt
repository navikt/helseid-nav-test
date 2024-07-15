package no.nav.helseidnavtest.edi20

import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.TokenExchangingRequestInterceptor
import no.nav.helseidnavtest.security.DPoPBevisGenerator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient.*


@Configuration(proxyBeanMethods = true)
class EDI20BeanConfig {

    @Bean
    @Qualifier(EDI20)
    fun edi20RestClient(b: Builder, cfg: EDI20Config, @Qualifier(EDI20) clientCredentialsRequestInterceptor: ClientHttpRequestInterceptor) =
        b.baseUrl("${cfg.baseUri}")
        .requestInterceptors {
           it.add(clientCredentialsRequestInterceptor)
       }.build()
}

@Component
@Qualifier(EDI20)
class DPoPEnabledTokenExchangingRequestInterceptor(
    private val generator: DPoPBevisGenerator, clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) : TokenExchangingRequestInterceptor(clientManager, DPOP) {
    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution) =
        with(req) {
            authorize(this)?.let {
                headers.set(DPOP.value, generator.bevisFor(method, uri, it.accessToken))
            }
            execution.execute(this, body)
        }
}




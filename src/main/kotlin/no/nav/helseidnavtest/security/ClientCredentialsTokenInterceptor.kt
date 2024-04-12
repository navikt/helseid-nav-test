package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
@Qualifier(ARBEID)
class ClientCredentialsTokenInterceptor(private val clientManager: OAuth2AuthorizedClientManager) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        clientManager.authorize(withClientRegistrationId(ARBEID)
                .principal("anonymous")
                .build()
        )?.let {
            request.headers.setBearerAuth(it.accessToken.tokenValue)
        }
        return execution.execute(request, body)
    }
}

@Component
@Qualifier(PDL)
class TokenInterceptor(private val clientManager: OAuth2AuthorizedClientManager) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction) =
        clientManager.authorize(withClientRegistrationId(PDL)
            .principal("anonymous")
            .build()
        )?.let { c ->
            next.exchange(
                ClientRequest.from(request)
                    .headers {
                        it.setBearerAuth(c.accessToken.tokenValue)
                    }.build()
            )
        } ?: next.exchange(request)
}
package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component

@Component
@Qualifier(ARBEID)
class ClientCredentialsRequestInterceptor(private val clientManager: OAuth2AuthorizedClientManager) : ClientHttpRequestInterceptor {

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
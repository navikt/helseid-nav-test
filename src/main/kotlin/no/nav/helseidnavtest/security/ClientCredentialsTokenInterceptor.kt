package no.nav.helseidnavtest.security

import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.*
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component

@Component
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
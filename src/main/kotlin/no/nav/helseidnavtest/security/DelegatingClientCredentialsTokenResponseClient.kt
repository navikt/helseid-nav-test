package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.stereotype.Component

@Component
class DelegatingClientCredentialsTokenResponseClient(private val dpopDelegate: DPoPClientCredentialsTokenResponseClient) :
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        if (request.isDPoP()) {
            dpopDelegate.getTokenResponse(request)
        } else {
            DefaultClientCredentialsTokenResponseClient().getTokenResponse(request)
        }.also {
            log.info("Received token response for : ${request.clientRegistration.registrationId}")
        }

    companion object {
        private fun OAuth2ClientCredentialsGrantRequest.isDPoP() = clientRegistration.registrationId.startsWith(EDI20)
        private val log = LoggerFactory.getLogger(DelegatingClientCredentialsTokenResponseClient::class.java)
    }
}
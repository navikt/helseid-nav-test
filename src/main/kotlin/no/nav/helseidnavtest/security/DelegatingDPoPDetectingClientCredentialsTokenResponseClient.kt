package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.stereotype.Component

@Component
class DelegatingDPoPDetectingClientCredentialsTokenResponseClient(private val delegate: DPoPClientCredentialsTokenResponseClient,
                                                                  private val detector: DPopDetector = object :
                                                                      DPopDetector {}) :
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private val log = LoggerFactory.getLogger(DelegatingDPoPDetectingClientCredentialsTokenResponseClient::class.java)

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        if (detector.isDPoP(request)) {
            delegate.getTokenResponse(request)
        } else {
            log.info("XXXXXXXXXXXXXXXXX")
            DefaultClientCredentialsTokenResponseClient().getTokenResponse(request)
        }
}

interface DPopDetector {
    fun isDPoP(req: AbstractOAuth2AuthorizationGrantRequest): Boolean =
        req.clientRegistration.registrationId.startsWith(EDI20)
}

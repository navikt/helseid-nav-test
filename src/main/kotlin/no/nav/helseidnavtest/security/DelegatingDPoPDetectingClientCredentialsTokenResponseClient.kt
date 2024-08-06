package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.PLAIN
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.stereotype.Component

@Component
class DelegatingDPoPDetectingClientCredentialsTokenResponseClient(@Qualifier(EDI20) private val dpop: OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest>,
                                                                  @Qualifier(PLAIN) private val plain: OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest>,
                                                                  private val detector: DPopDetector) :
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    override fun getTokenResponse(req: OAuth2ClientCredentialsGrantRequest) =
        if (detector.isDPoP(req)) {
            dpop.getTokenResponse(req)
        } else {
            plain.getTokenResponse(req)
        }
}

interface DPopDetector {
    fun isDPoP(req: AbstractOAuth2AuthorizationGrantRequest): Boolean

}

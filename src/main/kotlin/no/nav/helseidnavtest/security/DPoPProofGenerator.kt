package no.nav.helseidnavtest.security

import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.JWTClaimsSet.Builder
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.dpop.DPoPProofFactory.TYPE
import com.nimbusds.openid.connect.sdk.Nonce
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet.NONCE_CLAIM_NAME
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Component
import java.net.URI
import java.security.MessageDigest.getInstance
import java.time.Instant.now
import java.util.*
import java.util.Base64.getUrlEncoder
import java.util.Date.from

@Component
class DPoPProofGenerator(private val keyPair: ECKey) {

    fun proofFor(method: HttpMethod, uri: URI, token: OAuth2AccessToken? = null, nonce: Nonce? = null) =
        SignedJWT(jwsHeader(), claims(method, uri, token, nonce)).apply {
            sign(ECDSASigner(keyPair.toECPrivateKey()))
        }.serialize()

    private fun claims(method: HttpMethod, uri: URI, token: OAuth2AccessToken?, nonce: Nonce?) =
        Builder()
            .jwtID("${UUID.randomUUID()}")
            .issueTime(from(now()))
            .nonce(nonce)
            .ath(token)
            .htm(method)
            .htu(uri)
            .build()

    private fun jwsHeader() = JWSHeader.Builder(ES256)
        .type(TYPE)
        .jwk(keyPair.toPublicJWK())
        .build()

    companion object {
        private const val HTM_CLAIM_NAME = "htm"
        private const val HTU_CLAIM_NAME = "htu"
        private const val ATH_CLAIM_NAME = "ath"

        private fun OAuth2AccessToken.hash() =
            getUrlEncoder()
                .withoutPadding()
                .encodeToString(getInstance("SHA-256")
                    .digest(tokenValue.toByteArray()))

        private fun URI.stripQuery() =
            "${URI(scheme, getAuthority(), getPath(), null, getFragment())}"

        private fun Builder.htm(method: HttpMethod) =
            claim(HTM_CLAIM_NAME, method.name())

        private fun Builder.htu(uri: URI) =
            claim(HTU_CLAIM_NAME, uri.stripQuery())

        private fun Builder.nonce(nonce: Nonce?) =
            apply { nonce?.let { claim(NONCE_CLAIM_NAME, it.value) } }

        private fun Builder.ath(token: OAuth2AccessToken?) =
            apply { token?.let { claim(ATH_CLAIM_NAME, it.hash()) } }
    }
}
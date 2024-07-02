package no.nav.helseidnavtest.security

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse.SIGNATURE
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.openid.connect.sdk.claims.HashClaim.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Instant.now
import java.util.*
import java.util.Base64.*
import java.util.Date.from

@Component
class DPoPProofGenerator(private val keyPair: ECKey = keyPair()) {
    fun generer(method: HttpMethod, uri: URI, token: OAuth2AccessToken? = null, nonce: String? = null) =
        SignedJWT(jwsHeader(), claims(method.name(), uri, token, nonce)).apply {
            sign(ECDSASigner(keyPair.toECPrivateKey()))
        }.serialize()

    private fun claims(method: String, uri: URI, token: OAuth2AccessToken?, nonce: String? = null) = claimsBuilder(method, uri).apply {
        nonce?.let {
            claim("nonce", it)
            claim("jti", "${UUID.randomUUID()}")
        }
        token?.let {
            claim("ath", it.hash())
        }
       getMessageDigestInstance(ES256, P_256)
    }.build()

    private fun OAuth2AccessToken.hash() =  getUrlEncoder().withoutPadding().encodeToString(getMessageDigestInstance(ES256, P_256).digest(tokenValue.toByteArray())
    )

    private fun claimsBuilder(method: String, uri: URI) = JWTClaimsSet.Builder()
        .jwtID("${UUID.randomUUID()}")
        .issueTime(from(now()))
        .claim("htm", method)
        .claim("htu", uri.stripQuery())


    private fun URI.stripQuery() = URI(scheme, getAuthority(), getPath(), null, getFragment()).toString()

    private fun jwsHeader() = JWSHeader.Builder(ES256)
        .type(JOSEObjectType("dpop+jwt"))
        .jwk(keyPair.toPublicJWK())
        .build()

    companion object {
        private val log = LoggerFactory.getLogger(DPoPProofGenerator::class.java)
        fun keyPair()=
            ECKeyGenerator(P_256)
                .algorithm(Algorithm("EC"))
                .keyUse(SIGNATURE)
                .keyID("${UUID.randomUUID()}")
                .generate()
    }
}
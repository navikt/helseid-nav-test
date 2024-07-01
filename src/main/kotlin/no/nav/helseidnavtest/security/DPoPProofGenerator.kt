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
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import java.net.URI
import java.security.MessageDigest
import java.time.Instant.now
import java.util.*
import java.util.Date.from

@Component
class DPoPProofGenerator(private val keyPair: ECKey = keyPair()) {
    fun generer(method: HttpMethod, uri: URI, nonce: String? = null, tokenValue: String? = null) =
        SignedJWT(jwsHeader(), claims(method.name(), uri, nonce, tokenValue)).apply {
            sign(ECDSASigner(keyPair.toECPrivateKey()))
        }.serialize().also { log.info("Token value $tokenValue ga DPoP proof for $method $uri: $it")}

    private fun claims(method: String, uri: URI,nonce: String? = null, tokenValue: String? = null) = claimsBuilder(method, uri).apply {
        nonce?.let {
            claim("nonce", it)
            claim("jti", "${UUID.randomUUID()}")
        }
        tokenValue?.let {
            val hash = MessageDigest.getInstance("SHA-256").digest(it.toByteArray())
            claim("ath", Base64.getEncoder().withoutPadding().encodeToString(hash))
        }
    }.build()

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
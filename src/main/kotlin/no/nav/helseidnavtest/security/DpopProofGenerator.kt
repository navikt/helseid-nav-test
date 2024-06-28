package no.nav.helseidnavtest.security

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
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
import java.time.Instant.now
import java.util.Date.from
import java.util.UUID

class DpopProofGenerator(private val keyPair: ECKey = keyPair()) {
    fun generate(method: HttpMethod, uri: String, nonce: String? = null) =
        SignedJWT(jwsHeader(), claims(method.name(), uri, nonce)).apply {
            sign(ECDSASigner(keyPair.toECPrivateKey()))
        }.serialize().also { log.info("DPoP proof for $method $uri: $it")}

    private fun claims(method: String, uri: String, nonce: String?) = claimsBuilder(method, uri).apply {
        nonce?.let {
            claim("nonce", it)
            claim("jti", "${UUID.randomUUID()}")
        }
    }.build()

    private fun claimsBuilder(method: String, uri: String) = JWTClaimsSet.Builder()
        .jwtID("${UUID.randomUUID()}")
        .issueTime(from(now()))
        .claim("htm", method)
        .claim("htu", uri)


    private fun jwsHeader() = JWSHeader.Builder(JWSAlgorithm.ES256)
        .type(JOSEObjectType("dpop+jwt"))
        .jwk(keyPair.toPublicJWK())
        .build()

    companion object {
        private val log = LoggerFactory.getLogger(DpopProofGenerator::class.java)
        fun keyPair()=
            ECKeyGenerator(P_256)
                .algorithm(Algorithm("EC"))
                .keyUse(SIGNATURE)
                .keyID("${UUID.randomUUID()}")
                .generate()
    }
}
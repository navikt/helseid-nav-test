package no.nav.helseidnavtest.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import com.nimbusds.openid.connect.sdk.claims.HashClaim
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.security.ClaimsExtractor.*
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.APPROVALS
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.ASSURANCE_LEVEL
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.AUTHORIZATION
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.DESCRIPTION
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.HPR_DETAILS
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.HPR_NUMBER
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.PID
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.PROFESSION
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.REQUISITION_RIGHTS
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.SECURITY_LEVEL
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.SPECIALITIES
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.VALUE
import no.nav.helseidnavtest.security.ClaimsExtractor.HPRApproval.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import java.security.MessageDigest
import java.util.*
import kotlin.reflect.jvm.isAccessible

class ClaimsExtractorTest   {
    @Test
    fun testExtractClaims() {
        val claims = mapOf(
            HPR_DETAILS to mapOf(
                    APPROVALS to listOf(
                        mapOf(
                            PROFESSION to "LE",
                            AUTHORIZATION to mapOf("value" to "1", DESCRIPTION to "Autorisasjon"),
                            REQUISITION_RIGHTS to listOf(mapOf(VALUE to "1", DESCRIPTION to "Full rekvisisjonsrett")),
                            SPECIALITIES to emptyList<Map<String, String>>()
                        )
                    ),
                    "hpr_number" to "123456"
            ),
            PID to "12345678901",
            HPR_NUMBER to 123456, SECURITY_LEVEL to "4",
            ASSURANCE_LEVEL to "high",
            "given_name" to "Ola",
            "middle_name" to "mellom",
            "family_name" to "Nordmann"
        )
       val e = ClaimsExtractor(claims)
        assertThat(e.hprNumber).isEqualTo(HprId(123456))
        println(e.professions)
        println(e.securityLevel)
        println(e.navn)
        println(e.assuranceLevel)
        println(e.fnr)
       // println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(claims))
    }

   @Test
    fun ser() {
        val token = """
eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4NjY3RjkwREMxMUJGMDRCRDk0NjdEMUY5MTIwQzRBNDM0MEI0Q0YiLCJ4NXQiOiJlR1pfa053UnZ3UzlsR2ZSLVJJTVNrTkF0TTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2hlbHNlaWQtc3RzLnRlc3QubmhuLm5vIiwibmJmIjoxNzE5ODM3NDk4LCJpYXQiOjE3MTk4Mzc0OTgsImV4cCI6MTcxOTgzODA5OCwiYXVkIjoibmhuOm1zaCIsImNuZiI6eyJqa3QiOiJWUEFxY3pQbDZ1aWNKR1d3aWtKLVZDUjJnVU1rdTNTQ25HT2Z3c2VaX2V3In0sInNjb3BlIjpbIm5objptc2gvYXBpIl0sImNsaWVudF9pZCI6IjBlODUwODk4LTA1ZWMtNGFkMi1hMDI4LTViNTk4OGNlNzVkZCIsImNsaWVudF9hbXIiOiJwcml2YXRlX2tleV9qd3QiLCJoZWxzZWlkOi8vY2xhaW1zL2NsaWVudC9jbGFpbXMvb3JnbnJfcGFyZW50IjoiODg5NjQwNzgyIiwiaGVsc2VpZDovL2NsYWltcy9jbGllbnQvYW1yIjoicnNhX3ByaXZhdGVfa2V5IiwibmhuOm1zaC9jbGllbnQvY2xhaW1zL2hlcmlkIjoiODE0MjUxOSIsImhlbHNlaWQ6Ly9jbGFpbXMvY2xpZW50L2NsaWVudF9uYW1lIjoiQVJCRUlEUy0gT0cgVkVMRkVSRFNFVEFURU4gLSBNZWxkaW5nc3RqZW5lciBTSFAgUkVTVCBrbGllbnQgKE5BVi1IRUxTRS1URVNUMSkiLCJqdGkiOiJBNkYyMkI2MEE2NjJCOTc3MERFM0UzRjUyQUVDMkMzQiJ9.Rsuv1Mzdp2Sr2aHxcd8kNq0STOIwKVNM61o-9WCVFSfnGQG7S6MXxSqJdB_RtSAyJt2_qr-XoV4c4grT_OiFzrMQ7UWgAMxHXUHNEi-q4RRMW96YpgC7SCTgCmzGjAmv5gLtIDQgF1-GEJ4RvSoolnmOndu6yP7uBrq4L5g9ZsRRLWMXfQkBkg9ANhH7fXC48bE8AWA_GA_2PidQJ_l029VVXBDX35gu-XrvoPb1QULuyCN81tZFCMu9TaokKjGG-_wwLnuMsgGnrzjLRqtjPowglZUgFU0T09qpHtwbZcUQYSE_AkdhR9VOJ2qpf6eUV9JSMwB1HDQcBiAuj0vWdVCenFR9-KZ-sB38X0C4wkq1jOnC-tN-xOY_5cDroldhtUrZDlPaR2fqfIfs744jc5ZG6XcMfF8vB42ei0CEM_Izoil2ZamiHbntzd0oXNnSwupBfT3bTjnI6DTqIScxTom94wBRa1jqWGIJhQagwP_MEXxA_tVAzWwxgH4plq1u""".trimIndent()
       val hash = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
       val encoded = Base64.getUrlEncoder().encodeToString(hash)
       println("Encoded er $encoded")
       val hash1 = HashClaim.getMessageDigestInstance(ES256, P_256).digest(token.toByteArray())
       val encoded1 = Base64.getUrlEncoder().encodeToString(hash1)
       assertEquals(encoded, encoded1)
    }
}

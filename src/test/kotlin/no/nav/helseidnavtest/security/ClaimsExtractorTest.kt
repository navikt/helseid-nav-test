package no.nav.helseidnavtest.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JWSAlgorithm.ES256
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import com.nimbusds.openid.connect.sdk.claims.HashClaim
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.edi20.EDI20RestClientAdapter
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

class ClaimsExtractorTest {
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
            HPR_NUMBER to "123456", SECURITY_LEVEL to "4",
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

}

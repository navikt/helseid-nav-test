package no.nav.helseidnavtest.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.oauth2.sdk.AccessTokenResponse
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
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

    private val x = """
        {"access_token":"eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4NjY3RjkwREMxMUJGMDRCRDk0NjdEMUY5MTIwQzRBNDM0MEI0Q0YiLCJ4NXQiOiJlR1pfa053UnZ3UzlsR2ZSLVJJTVNrTkF0TTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2hlbHNlaWQtc3RzLnRlc3QubmhuLm5vIiwibmJmIjoxNzE5NTY1MjQwLCJpYXQiOjE3MTk1NjUyNDAsImV4cCI6MTcxOTU2NTg0MCwiYXVkIjoibmhuOm1zaCIsImNuZiI6eyJqa3QiOiI1eTZpN3k1OVU3eER5S3Z2emNZOE5weWxnWU9HRDFTNFVUSWVaQVZpREo4In0sInNjb3BlIjpbIm5objptc2gvYXBpIl0sImNsaWVudF9pZCI6IjBlODUwODk4LTA1ZWMtNGFkMi1hMDI4LTViNTk4OGNlNzVkZCIsImNsaWVudF9hbXIiOiJwcml2YXRlX2tleV9qd3QiLCJoZWxzZWlkOi8vY2xhaW1zL2NsaWVudC9jbGFpbXMvb3JnbnJfcGFyZW50IjoiODg5NjQwNzgyIiwiaGVsc2VpZDovL2NsYWltcy9jbGllbnQvYW1yIjoicnNhX3ByaXZhdGVfa2V5IiwibmhuOm1zaC9jbGllbnQvY2xhaW1zL2hlcmlkIjoiODE0MjUxOSIsImhlbHNlaWQ6Ly9jbGFpbXMvY2xpZW50L2NsaWVudF9uYW1lIjoiQVJCRUlEUy0gT0cgVkVMRkVSRFNFVEFURU4gLSBNZWxkaW5nc3RqZW5lciBTSFAgUkVTVCBrbGllbnQgKE5BVi1IRUxTRS1URVNUMSkiLCJqdGkiOiJGOEM3NjNBMTVCQ0UxQjcwNjdBMDQ5Q0FENUYxNjFFNSJ9.MTQZBxVnY4Dvjc1z2PxPH-D6CFxMX8wTEG0y5AMA-Oyuq-qJD_stbbKOPXJUfyxGNwct_LuUp60PsaammWtL9ObE9fHrdKS4BIh1uGhq0iAJDUlhyuoWH_Bg1wDszOsjGC4WTOaCyDW-b6Qe-_Cwaxwn-A6OmZyyr3cfQlLlbcmo43pNqrmKi6-Ozsij3RZnFjVnLlGOLUL5xXbn14Di7DOWyo_5Zsolv9O2yYBhCFzdvchTe7UORrPJD8AECJmwdws2gogFe_7iNjZ-wcbj4X4FXZBxIPIScjJcYnmOxUPc5mEdzO6xtVsjZVVncAmVGQ7eXlIHxpqP-e3NlnMsBzvPvgSVZWbT3-KDEDpFkL6WTSase6FD6PA9VaJwMwT-KLfOqAyB6k-vKOHECVxsslmoy-I4gcPRKhdpXnS-Sl0ST1ebox4Yn_UTKtjb_zhd7dpZwEnF3WjhbRLp4uGSMpF3iJ2IbXvpJsApKL3ocd79WB1CHJB1sghgzkZECdwN","expires_in":600,"token_type":"DPoP","scope":"nhn:msh/api"}
    """.trimIndent()

   @Test
    fun ser() {
        val token = """
eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4NjY3RjkwREMxMUJGMDRCRDk0NjdEMUY5MTIwQzRBNDM0MEI0Q0YiLCJ4NXQiOiJlR1pfa053UnZ3UzlsR2ZSLVJJTVNrTkF0TTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2hlbHNlaWQtc3RzLnRlc3QubmhuLm5vIiwibmJmIjoxNzE5ODM1NjkwLCJpYXQiOjE3MTk4MzU2OTAsImV4cCI6MTcxOTgzNjI5MCwiYXVkIjoibmhuOm1zaCIsImNuZiI6eyJqa3QiOiJuMlhkaFVoT3hsUE5Zdm5RZEpGWTYySmZmdkF2R1pTVDFYM3ItbmdqckxnIn0sInNjb3BlIjpbIm5objptc2gvYXBpIl0sImNsaWVudF9pZCI6IjBlODUwODk4LTA1ZWMtNGFkMi1hMDI4LTViNTk4OGNlNzVkZCIsImNsaWVudF9hbXIiOiJwcml2YXRlX2tleV9qd3QiLCJoZWxzZWlkOi8vY2xhaW1zL2NsaWVudC9jbGFpbXMvb3JnbnJfcGFyZW50IjoiODg5NjQwNzgyIiwiaGVsc2VpZDovL2NsYWltcy9jbGllbnQvYW1yIjoicnNhX3ByaXZhdGVfa2V5IiwibmhuOm1zaC9jbGllbnQvY2xhaW1zL2hlcmlkIjoiODE0MjUxOSIsImhlbHNlaWQ6Ly9jbGFpbXMvY2xpZW50L2NsaWVudF9uYW1lIjoiQVJCRUlEUy0gT0cgVkVMRkVSRFNFVEFURU4gLSBNZWxkaW5nc3RqZW5lciBTSFAgUkVTVCBrbGllbnQgKE5BVi1IRUxTRS1URVNUMSkiLCJqdGkiOiJCRTJERDQ5RTZCMUI5MUE5MzZCRjZEMjkwRjVDRjhEMyJ9.X1DgFCX5HiRdCV0Eir2g_ed-C0bvLd3MPT_jIOfTLXOFQH4k34samXi-D3HshcBFegPgVj8BxFZ5YROMymbOWMmThCza9ShWKOrehztSg0WKPr10xa9q1J5KSD2DBzAVCtm9Qss5aONr6-7vW9yPx2WjVzirdpPYPwEYcj_z-7INundv0zJKPN17DEVL_W2DB3fzbSIryXMH6Cfidx5ycJWhIORixvA1S5tbCENKdgrbZgLnOdxF2PeCk_gx2S5v5Oxu1IkE6s9WnjdPAACS_YAj075pLnNseLzX-AitzMfxFJBSWOxJ9HwrmU3IQAObhdzaUvEYziotfkCVgm8sflLu_Iyn6UyzKY4BwOFYQ0JjKC-YK5FykhhUScvscaMJuCglTGm4LaOPgNU7PE6Sp7y3wJvc07npE7hJd6EMsIsQS8slkIUOdcunQ0ck_suZLHIk_MT3xYADhWZaSwpqiuUAhKOrf94jfLVE-z843yANYzZfVU-K35MIi4U75gqr        """.trimIndent()
       val hash = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
       val encoded = Base64.getEncoder().withoutPadding().encodeToString(hash)
       println("Encoded er $encoded")
    }
}


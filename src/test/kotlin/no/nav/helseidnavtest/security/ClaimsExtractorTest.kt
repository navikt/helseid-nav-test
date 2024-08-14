package no.nav.helseidnavtest.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import no.nav.helseidnavtest.dialogmelding.HprId
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
import org.assertj.core.api.Assertions.assertThat
import java.net.URI

class ClaimsExtractorTest {

    val assertion = """
        {
          "d": "ANpEFxkoEcw4cMkYQrD3KBKhe5nslAGNsZPkZKxSvrv_TgfZVg_Y1mVLxPDJJAcP5sxZn2QTqr947QU2tbjQH4g8LuN-qvxf1gYIFHZRyQBhAjppai4nP-uBHpPWAVubg2oO5R7MnE6AYA3nK-JAjQpbTy7xcKkPz6csk5sI7htJOTYkUcFu37mM8M7r6a2x_8EmaaqcEBhbBarM-46Ws8v3bf-ssWJeaGZH7htb1t4rdFHBad0HD3C0M85-qVyTw7YBMB-wW-FjNuow626B0EkXdf2EMkwxm-vGkzlZYVO8hka1SvdUnTuqW1UxcrOXNvmSiG51Qh6tksVFvGXc1p4Um1uO04aKRRDktg33HTaIcpwhHWLs_6wCLz8EAGV4ik5zpf0WpnUHC4MtE1UdiBnQhakZ6OVbhZHKSJmLzdG5YH0GKJyH_owQ4XBHu7LbzhquLQsmhLOBhSKBB0PbU_lM-pD6A3CWRgmVc5N4z_CQ-3sLpZuDDqpX4FXeYt8ci5cKaK67ts_7xX4Rt_Qz-r65mvkPIE9AZIxdaZ5eVycghc8j7UnMu4fvkP7B9zB2OVICUgUv6GZVT6OK6S2pYYO3T_KFC3EcYcho0jdqvk5ImDTEAKJozV0FejHYfZrfBaDba6abzFQcD2jL1NsevFiGfz1gjTheN5S7OCW8LJE",
          "dp": "4i___XrRaRz00AUht-uRsSIxld1SwdMTxWHkB1sAI9P05HS0oLgdvY3D_PpTJUYDU8oD1UGeRvk5KEhS7zbHiQnRGE9IqvorPEM8IBOc1d2TtdGZPJ4Gyu2T9j7o8tDLlC8-wG1lj61REHzq8RZa_3teDNgsmS0VezMIdrWIpkCBjyecwGedkA9qpcgmm8eryLOjLqiTPXA4EaomVnAWRcNoE_btFtOSgnw31KfcKNSbtoLVkPtuKsii9woquK6giRotrHjLWnPBt1sKbNyUEtd9BzN4QGhQT9ADCAy9oB82ndizVsrZoJmTaJm5_M4I2863FlPfqUcw7KWGd5pXPw",
          "dq": "DY4eDcVQOUNA0gsrHTOfKSBunhbye_cTNNG18mZ6jLssQMt301zrlJsgMwCSzsGyZrs0SdCb7Qn_K2i62XVR3J9FrBPKOHs6lKBVssjh9H-_xbnYHopc7GBVmYdVxwMcQ4km7D9twsSioBBS8EmLQNr6ZoPARcCKOdFS-WNAyyTYLroqzTzf-04wNh1gxQ-vMKKcoBc2ufrQuCnEf-eKwU2pY2Q41qk0hWY3MBChTYwZfw2uuF3togOROZ3J62Y2XssEfnJjlD0g1SGpwZTqWNDnD25Zsr55nsAU_oq7G1EaZGOiMhn5SiLWOaUzqKUajtNZusZnfCu2hOLvEW8p1Q",
          "e": "AQAB",
          "kty": "RSA",
          "n": "okKx_Yme-xVwm-VHq6sr8-ZMWcICAqv5DiT01GR6PVGH6Q2U9vOfifif7x1ZQKiL3Mq1WyBYRRh1Gnt5lt3M8DMK8G3WO0Vzd1tET9URIP3E9rJHCe01CRDOwcT2KQGv0T08gfR3E_qn2FnpMBupNU3QIPHuaEXMUVrdcLD9oprMv2-wLjns4IRzlMCq_thqj2Dgsmheq-I3J_a3C5-lsCmZQfO1ZHPIgTUdNXWf2MI1voV4BNosiS9kQBk9ZTEBiogcN3SRg2ql4daZNfpGb7wXK45DbEAaPXXXYoDruDeSi9wHUdpwMz0JiPIk6mgHeIDz-wuZiYVX5wBkfTMaLqZnmeG0aF-ZVX3QKWjlyP0uC6TNEbD_E6ddslrrcZGgnYCKBnoTujjgzKpKQ7bWLyFBCmG9iNf1LR3cBKp4YHoHiLmAKygaqQroat5Dl7Gn4ZDQhNJaLoTs3HAhsG9_Jkh8hWCN8MFY992PPFnjI1k7qwKNzQFGSKPcwFXpe___cQ6CAPLgOg3O6X4F4OTsoOerkemn5Cp5KvNgXIY_pZ_AcoymQ9zEaWrnfxoWdkADIN-N7O6e-02itozAoXIeElSJ5dxMNh8jRcqkmPouiQKA6X4AL2C_PwWPZ5PQ4with4bcieALUDXNpJKWHhpnA8dtdG4X5-rAz9a241oiqgs",
          "p": "5OI3epDxawOp9dlfZ2Gkr7EE2ap0_X1F4e0Wc939D_s6WSQQs7oHuvONDIsXBfAKiYmQ7sSNfrww6M6hiAZh-OHqh3ZabJfjqRSxtoh58LprA7FubcvwnqIhLb-nZuSg9b7w_32dTdRHD9KizlS1M5CqON-Cl6hu7mBwG2By1aKQaJQiCoob6b3xxFUYrUAPfwsKIc-WFwrbNBAEPVs2ar0lf01nK3iVgFArxLESGjW0SRarWeCu1MGQtoLNtF-VFbspdIBWV0f58N0r4xPl3LM_t5Zpc3dREUdcxHNtTxH54J7Peluylk3gjXcwjcL32k4L8wfmyTbaZ5x_cPH3lw",
          "q": "tXvfx0aM0SprEfAOqbGtGBaUWFx-S7RwHze82taTwOnt5zIvvU5xxrfRdhPdB5wQ948xlGrHjUDpvf6MlmstBpi58EhKsjQlBQQmBu8KsFWS-6OiF6n-XXhxD8ppCHdpT2kXWI8xbqGsrFHzlcFSmY9PhOXW-OCsFHo1zMJ0VQSei8JeNdmtRglIqHvIsQBGTjpTgTh4-dttgu2SyLkQ1Pr2ls7MoxF6JecIObpOP1qv_d14p5j478xKHcIowAvOrtp3ALnej9tyfpZ0y3mxv2y4vCNppb4ExVuqXjojJZ0JVbfb5nzAdp57jcAo46RU7b484bnvFd77y5GLL1ePrQ",
          "qi": "bzgB4cM8Y0rQF6GoHgkEGAEfIGY8wkByKLlMDrJ1VOK0HVp2M0I8tYsKcMIxdWkRDywvx2Qu_ClXNSpcRRkB6mlglj2gULWkxLw4plXlAZg5gpRzUyKk-zyE1K7u2VuzahwHQoZTEFRji9pe_DbS8c9QXAEFsvI2IF9ILohlBZER-7Z9Bp3WRkxSGqWOr6vjoKugZP2T5NJojH9vTf7f7vbAnUJWARJB52cvUAoGHJ5H14QTAa-uPzQP9xj32jlT3g16spNNLxQFLZeyKFoA8jYhgEKPRJQBdTSkVa7JxSeI35quebBY1eGx_mzhQvGrMBRF3rbh-kGgrnEOB3_KWg"
        }
    """.trimIndent()

    // @Test
    fun xxx() {
        val key = JWK.parse(assertion).toRSAKey()
        val signer = RSASSASigner(key)
        val claimsSet = JWTClaimsSet.Builder()
            .subject("example-subject")
            .issuer("example-issuer")
            .build()

        // Create the JWS object
        val jwsObject = JWSObject(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.keyID).build(),
            Payload(claimsSet.toJSONObject()))


        jwsObject.sign(signer)

        val endpoint = URI.create("https://helseid-sts.test.nhn.no/.well-known/openid-configuration")

        // Construct an OAuth 2.0 authorisation request as usual
        val authzRequest = AuthorizationRequest.Builder(
            ResponseType("code"),
            ClientID("0e850898-05ec-4ad2-a028-5b5988ce75dd"))
            .redirectionURI(URI.create("https://example.com/cb"))
            //  .scope(Scope("upload_doc"))
            .state(State())
            .build()

            // val httpRequest = PushedAuthorizationRequest(endpoint, clientAuth, authzRequest)
            .toHTTPRequest()
        /*
        val httpResponse = httpRequest.send()

        // Process the PAR response
        val response: PushedAuthorizationResponse = PushedAuthorizationResponse.parse(httpResponse)

        if (!response.indicatesSuccess()) {
            System.err.println("PAR request failed: " + response.toErrorResponse().errorObject.httpStatusCode)
            System.err.println("Optional error code: " + response.toErrorResponse().errorObject.httpStatusCode)
            return
        }

        val successResponse = response.toSuccessResponse()
        println("Request URI: " + successResponse.requestURI)
        println("Request URI expires in: " + successResponse.lifetime + " seconds")
         */
        // The client authenticates the same way as it's supposed at the

    }

    // @Test
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

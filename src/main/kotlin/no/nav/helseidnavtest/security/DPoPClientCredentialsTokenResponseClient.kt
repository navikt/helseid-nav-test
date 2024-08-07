package no.nav.helseidnavtest.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import com.nimbusds.openid.connect.sdk.Nonce
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatusCode
import org.springframework.http.RequestEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.net.URI
import kotlin.reflect.jvm.isAccessible

@Component
@Qualifier(EDI20)
class DPoPClientCredentialsTokenResponseClient(
    @Qualifier(EDI20) restTemplate: RestTemplate,
    private val generator: DPoPProofGenerator,
    private val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>,
    private val mapper: ObjectMapper) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private val restClient = RestClient.builder(restTemplate).build()

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let(::getResponse)
            ?: authErrorRequest("No request entity")

    private fun getResponse(req: RequestEntity<*>) =
        with(req) {
            body?.let {
                log.info("Requesting DPoP token from $url")
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.apply {
                            addAll(headers)
                            add(DPOP.value, generator.proofFor(POST, url))
                        }
                    }
                    .body(it)
                    .exchange(noNonce(this))
            } ?: authErrorResponse("No body in response", null, req.url)
        }

    private fun noNonce(request: RequestEntity<*>) = { req: HttpRequest, res: ClientHttpResponse ->
        with(res) {
            if (BAD_REQUEST == statusCode && headers[DPOP_NONCE] != null) {
                runCatching {
                    retryWithNonce(request, nonce(this))
                }.getOrElse {
                    if (it is OAuth2AuthorizationException) throw it
                    authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri, it)
                }
            } else {
                authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri)
            }
        }
    }

    private fun nonce(res: ClientHttpResponse) =
        res.headers[DPOP_NONCE]?.let {
            Nonce(it.single())
        } ?: authErrorResponse("No nonce in response from token endpoint", res.statusCode)

    private fun retryWithNonce(req: RequestEntity<*>, nonce: Nonce) =
        with(req) {
            body?.let { b ->
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.addAll(headers).also { log.info("FLyttet headere $headers") }
                        it.add(DPOP.value, generator.proofFor(POST, req.url, nonce = nonce))
                    }
                    .body(b)
                    .exchange(exchangeEtterNonce())
            } ?: authErrorResponse("No body in request", null, req.url)
        }

    private fun exchangeEtterNonce() = { req: HttpRequest, res: ClientHttpResponse ->
        with(res) {
            if (!statusCode.is2xxSuccessful) {
                authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri)
            }
            runCatching {
                deserialize(this)
            }.getOrElse {
                authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri, it)
            }
        }

    }

    private fun deserialize(res: ClientHttpResponse) =
        mapper.readValue<Map<String, Any>>(res.body).run {
            OAuth2AccessTokenResponse.withToken(this["access_token"] as String)
                .expiresIn((this["expires_in"] as Int).toLong())
                .scopes(setOf(this["scope"] as String))
                .tokenType(DPOP_TOKEN_TYPE)
                .additionalParameters(this)
                .build()
        }

    companion object {
        private val DPOP_TOKEN_TYPE = TokenType::class.constructors.single().run {
            isAccessible = true
            call(DPOP.value)
        }

        private fun authErrorResponse(txt: String,
                                      code: HttpStatusCode? = null,
                                      uri: URI? = null,
                                      e: Throwable? = null): Nothing =
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_RES, "$txt $code", "$uri"), e)

        private fun authErrorRequest(txt: String, uri: URI? = null): Nothing =
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_REQ, txt, "$uri"))

        const val DPOP_NONCE = "dpop-nonce"
        const val INVALID_RES = "invalid_token_response"
        const val INVALID_REQ = "invalid_token_request"

        private val log =
            LoggerFactory.getLogger(DelegatingDPoPDetectingClientCredentialsTokenResponseClient::class.java)
    }
}
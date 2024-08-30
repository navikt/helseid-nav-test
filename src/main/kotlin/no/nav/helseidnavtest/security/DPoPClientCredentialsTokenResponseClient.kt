package no.nav.helseidnavtest.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import com.nimbusds.openid.connect.sdk.Nonce
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.PLAIN
import org.slf4j.LoggerFactory.getLogger
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
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse.withToken
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.net.URI
import kotlin.reflect.jvm.isAccessible

@Component
@Qualifier(EDI20)
class DPoPClientCredentialsTokenResponseClient(
    @Qualifier(PLAIN) private val restClient: RestClient,
    private val generator: DPoPProofGenerator,
    private val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>,
    private val mapper: ObjectMapper) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let(::getResponse)
            ?: authErrorRequest("No request entity")

    private fun getResponse(req: RequestEntity<*>) =
        with(req) {
            body?.run {
                log.info("Requesting DPoP token from $url")
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.apply {
                            addAll(headers)
                            add(DPOP.value, generator.proofFor(POST, url))
                        }
                    }
                    .body(this)
                    .exchange(withoutNonce(this@with))
            } ?: authErrorResponse("No body in response", null, req.url)
        }

    private fun withoutNonce(request: RequestEntity<*>) = { req: HttpRequest, res: ClientHttpResponse ->
        with(res) {
            Nonce(headers[DPOP_NONCE]?.single()).let {
                runCatching {
                    check(BAD_REQUEST == statusCode,
                        { "Unexpected response ${statusCode} from token endpoint ${req.uri}" })
                    withNonce(request, it)
                }.getOrElse {
                    when (it) {
                        is IllegalStateException -> authErrorResponse("Unexpected response code",
                            res.statusCode,
                            req.uri,
                            it)

                        is IllegalArgumentException -> authErrorResponse("Multiple nonces in response",
                            BAD_REQUEST,
                            req.uri,
                            it)

                        is OAuth2AuthorizationException -> throw it
                        else -> authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri, it)
                    }
                }
            }
        }
    }

    private fun withNonce(req: RequestEntity<*>, nonce: Nonce) =
        with(req) {
            log.info("Received nonce ${nonce.value} in response, retrying")
            body?.run {
                restClient
                    .method(POST)
                    .uri(url)
                    .headers { headers ->
                        headers.add(DPOP.value, generator.proofFor(POST, url, nonce = nonce))
                    }
                    .body(this)
                    .exchange(response())
            } ?: authErrorResponse("No body in request", null, req.url)
        }

    private fun response() = { req: HttpRequest, res: ClientHttpResponse ->
        with(res) {
            if (!statusCode.is2xxSuccessful) {
                authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri)
            }
            runCatching {
                log.info("Exchange after nonce successful, deserialize response")
                deserialize(body)
            }.getOrElse {
                authErrorResponse("Unexpected response from token endpoint", statusCode, req.uri, it)
            }
        }
    }

    private fun deserialize(body: InputStream) =
        mapper.readValue<Map<String, Any>>(body).run {
            withToken(this[ACCESS_TOKEN] as String)
                .expiresIn((this[EXPIRES_IN] as Int).toLong())
                .scopes(setOf(this[SCOPE] as String))
                .tokenType(DPOP_TOKEN_TYPE)
                .additionalParameters(this)
                .build().also { log.info("Deserialize ok to $it") }
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

        private val log = getLogger(DelegatingDPoPDetectingClientCredentialsTokenResponseClient::class.java)
    }
}
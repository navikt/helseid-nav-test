package no.nav.helseidnavtest.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.RequestEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate


class DpopEnabledClientCredentialsTokenResponseClient(private val generator: DpopProofGenerator, val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {


    private val restOperations = RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter())).apply {
        setRequestFactory(HttpComponentsClientHttpRequestFactory())
        errorHandler = OAuth2ErrorResponseErrorHandler()
    }

    private val restClient = RestClient.create(restOperations)
    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let {
            log.info("Request converted to ${it.body} ${it.headers} ${it.url}")
            getResponse(it)
        } ?: throw OAuth2AuthorizationException(OAuth2Error("invalid_request", "Request could not be converted", null))

    private fun getResponse(request: RequestEntity<*>): OAuth2AccessTokenResponse {
        log.info("Requesting token from ${request.url}")
        return restClient.method(POST)
            .uri(request.url)
            .headers {
                it.addAll(request.headers)
                it.add("DPoP",generator.generate(POST, "${request.url}"))
            }
            .body(request.body!!)
            .exchange { req, res ->
                if (res.statusCode.value() == BAD_REQUEST.value() && res.headers["dpop-nonce"] != null) {
                    val nonce = res.headers["dpop-nonce"]!!.first()
                    log.info("Token require nonce $nonce from token endpoint: ${res.statusCode}")
                    val nyttproof = generator.generate(POST, "${req.uri}", nonce)
                    dpopProof.set(nyttproof)
                    try {
                        restClient.method(POST)
                            .uri(request.url)
                            .headers {
                                it.addAll(request.headers)
                                it.add("DPoP",nyttproof)
                            }
                            .body(request.body!!)
                            .exchange { _, res2 ->
                                if (res2.statusCode.value() in 200..299) {
                                    log.info("Got token sucessfully from second shot token endpoint ${res2.statusCode}")
                                    try {
                                        val m = jacksonObjectMapper().readValue(res2.body, STRING_OBJECT_MAP)
                                        log.info("2 Converted response to map $m")
                                        OAuth2AccessTokenResponse.withToken(m["access_token"] as String)
                                            .expiresIn((m["expires_in"] as Int).toLong())
                                            .scopes(setOf(m["scope"] as String))
                                            .tokenType(BEARER) // TODO dette er jo feil?
                                            .additionalParameters(m)
                                            .build().also {
                                            log.info("2 Converted response to OAuth2AccessTokenResponse $it")
                                        }
                                    }
                                    catch (e: Exception) {
                                        log.info("2 Failed to convert response to OAuth2AccessTokenResponse",e)
                                        throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from token endpoint: ${res2.statusCode} ${res2.body}", req.uri.toString()))
                                    }
                                } else {
                                    log.info("Unexpected response ${res2.statusCode} from second shot token endpoint")
                                    throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from token endpoint: ${res2.statusCode} ${res2.body}", req.uri.toString()))
                                }
                            }
                    }
                    catch (e: Exception) {
                        log.info("Unexpected response from second shot token endpoint: ${res.statusCode}",e)
                        throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
                    }
                }
                else  {
                    log.info("Unexpected response from first shot token endpoint: ${res.statusCode}")
                    throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from first shot token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
                }
            }
    }

    companion object {
        val dpopProof = ThreadLocal<String>()
        val STRING_OBJECT_MAP = object : TypeReference<Map<String, Any>>() {}
        private const val INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DpopEnabledClientCredentialsTokenResponseClient::class.java)
    }
}

package no.nav.helseidnavtest.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import com.nimbusds.openid.connect.sdk.Nonce
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import kotlin.reflect.jvm.isAccessible


@Component
class DPoPClientCredentialsTokenResponseClient(private val generator: DPoPBevisGenerator, val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {


    private val restOperations = RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter())).apply {
        setRequestFactory(HttpComponentsClientHttpRequestFactory())
        errorHandler = OAuth2ErrorResponseErrorHandler()
    }

    private val restClient = RestClient.create(restOperations)
    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let {
            if (request.clientRegistration.registrationId.startsWith("edi20")) {
                log.info("1 Requesting edi 2.0 token from ${it.url}")
                getDPoPResponse(it)
            } else {
                log.info("1 Requesting vanilla token from ${it.url}")
                getVanillaResponse(it).body
              }
        } ?: throw OAuth2AuthorizationException(OAuth2Error("invalid_request", "Request could not be converted", null))

    private fun getVanillaResponse(request: RequestEntity<*>): ResponseEntity<OAuth2AccessTokenResponse> {
        try {
            return restOperations.exchange(request, OAuth2AccessTokenResponse::class.java)
        } catch (e: RestClientException) {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${e.message}", null), e)
        }
    }

    private fun getDPoPResponse(request: RequestEntity<*>) =
        restClient.method(POST)
            .uri(request.url)
            .headers {
                it.addAll(request.headers)
                it.add(DPOP.value,generator.bevisFor(POST, request.url))
            }
            .body(request.body!!)
            .exchange(førNonce(request))

    private fun førNonce(request: RequestEntity<*>) = { req: HttpRequest, res:  ClientHttpResponse->
        if (res.statusCode == BAD_REQUEST && res.headers[DPOP_NONCE] != null) {
            val nonce = res.headers[DPOP_NONCE]?.let {
                Nonce(it.single())
            }
            try {
                tryAgainWithNonce(request, req, nonce)
            } catch (e: Exception) {
                if (e is OAuth2AuthorizationException) throw e
                throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
            }
        } else {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from first shot token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
        }
    }

    private fun tryAgainWithNonce(request: RequestEntity<*>, req: HttpRequest, nonce: Nonce?) =
        restClient.method(POST)
            .uri(request.url)
            .headers {
                it.addAll(request.headers)
                it.add(DPOP.value, generator.bevisFor(POST, req.uri, nonce = nonce))
            }
            .body(request.body!!)
            .exchange(exchangeEtterNonce())

    private fun exchangeEtterNonce() = { req: HttpRequest, res: ClientHttpResponse ->
        if (!res.statusCode.is2xxSuccessful) {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Unexpected response from token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
        }
        try {
            val m = jacksonObjectMapper().readValue(res.body, STRING_OBJECT_MAP)
            log.info("2 Converted response to map $m")
            OAuth2AccessTokenResponse.withToken(m["access_token"] as String)
                .expiresIn((m["expires_in"] as Int).toLong())
                .scopes(setOf(m["scope"] as String))
                .tokenType(dPoPTokenType())
                .additionalParameters(m)
                .build()
        } catch (e: Exception) {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "Error response from token endpoint: ${res.statusCode}", req.uri.toString()))
        }
    }

    private fun dPoPTokenType()=
        TokenType::class.constructors.single().run {
            isAccessible = true
            call(DPOP.value)
        }


    companion object {
        const val DPOP_NONCE = "dpop-nonce"
        val STRING_OBJECT_MAP = object : TypeReference<Map<String, Any>>() {}
        private const val INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DPoPClientCredentialsTokenResponseClient::class.java)
    }
}

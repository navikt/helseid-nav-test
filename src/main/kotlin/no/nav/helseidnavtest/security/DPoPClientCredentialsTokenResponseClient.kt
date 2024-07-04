package no.nav.helseidnavtest.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import org.springframework.web.client.RestTemplate
import kotlin.reflect.jvm.isAccessible


@Component
class DPoPClientCredentialsTokenResponseClient(
    private val generator: DPoPBevisGenerator,
    private val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>,
    private val mapper: ObjectMapper
) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private val restOperations =
        RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter())).apply {
            setRequestFactory(HttpComponentsClientHttpRequestFactory())
        }

    private val restClient = RestClient.builder(restOperations)
        .defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
        .build()

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let {
            if (request.isDPoP()) {
                log.info("Requesting DPoP token from ${it.url}")
                dPoPTokenResponse(it)
            } else {
                log.info("Requesting vanilla token from ${it.url}")
                vanillaTokenResponse(it).body
            }
        } ?: throw OAuth2AuthorizationException(OAuth2Error("invalid_request", "Request could not be converted", null))


    private fun OAuth2ClientCredentialsGrantRequest.isDPoP() = clientRegistration.registrationId.startsWith("edi20")

    private fun vanillaTokenResponse(request: RequestEntity<*>): ResponseEntity<OAuth2AccessTokenResponse> {
        runCatching {
            return restOperations.exchange(request, OAuth2AccessTokenResponse::class.java)
        }.getOrElse {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${it.message}", null), it)
        }
    }

    private fun dPoPTokenResponse(request: RequestEntity<*>) =
        with(request) {
            body?.let {
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.addAll(headers)
                        it.add(DPOP.value, generator.bevisFor(POST, url))
                    }
                    .body(it)
                    .exchange(førNonce(this))
            } ?: throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "No body in request", request.url.toString()))
        }

    private fun førNonce(request: RequestEntity<*>) = { req: HttpRequest, res: ClientHttpResponse ->
        if (res.statusCode == BAD_REQUEST && res.headers[DPOP_NONCE] != null) {
            runCatching {
                medNonce(request, req, nonce(res))
            }.getOrElse {
                if (it is OAuth2AuthorizationException) throw it
                throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "Error response from token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()), it)
            }
        } else {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "Error response from first shot token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
        }
    }

    private fun nonce(res: ClientHttpResponse) =
        res.headers[DPOP_NONCE]?.let {
            Nonce(it.single())
        } ?: throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "No nonce in response from token endpoint", null))

    private fun medNonce(request: RequestEntity<*>, req: HttpRequest, nonce: Nonce?) =
        with(request) {
            body?.let {
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.addAll(headers)
                        it.add(DPOP.value, generator.bevisFor(POST, req.uri, nonce = nonce))
                    }
                    .body(it)
                    .exchange(exchangeEtterNonce())
            }
        } ?: throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "No body in request", request.url.toString()))

    private fun exchangeEtterNonce() = { req: HttpRequest, res: ClientHttpResponse ->
        if (!res.statusCode.is2xxSuccessful) {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "Unexpected response from token endpoint: ${res.statusCode} ${res.body}", req.uri.toString()))
        }
        runCatching {
            deserialize(res)
        }.getOrElse {
            throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE, "Error response from token endpoint: ${res.statusCode}", req.uri.toString()), it)
        }
    }

    private fun deserialize(res: ClientHttpResponse) =
        mapper.readValue<Map<String, Any>>(res.body).run {
            OAuth2AccessTokenResponse.withToken(this["access_token"] as String)
                .expiresIn((this["expires_in"] as Int).toLong())
                .scopes(setOf(this["scope"] as String))
                .tokenType(dPoPTokenType())
                .additionalParameters(this)
                .build()
        }


    companion object {
        private fun dPoPTokenType() =
            TokenType::class.constructors.single().run {
                isAccessible = true
                call(DPOP.value)
            }

        private val MAPPER = jacksonObjectMapper()
        const val DPOP_NONCE = "dpop-nonce"
        private const val INVALID_RESPONSE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DPoPClientCredentialsTokenResponseClient::class.java)
    }
}
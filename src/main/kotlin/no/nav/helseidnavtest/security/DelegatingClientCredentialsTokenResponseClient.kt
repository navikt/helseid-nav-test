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
import org.springframework.http.RequestEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
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
class DPoPClientCredentialsTokenResponseClient(
    @Qualifier(EDI20) restTemplate: RestTemplate,
    private val generator: DPoPBevisGenerator,
    private val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>,
    private val mapper: ObjectMapper) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private val restClient = RestClient.builder(restTemplate).build()

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let(::getResponse)
            ?: throw OAuth2AuthorizationException(OAuth2Error("invalid_request"))

    private fun getResponse(request: RequestEntity<*>) =
        with(request) {
            body?.let {
                log.info("Requesting DPoP token from ${request.url}")
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.apply {
                            addAll(headers)
                            add(DPOP.value, generator.bevisFor(POST, url))
                        }
                    }
                    .body(it)
                    .exchange(utenNonce(this))
            } ?: throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE,
                "No body in request",
                request.url.toString())
            )
        }

    private fun utenNonce(request: RequestEntity<*>) = { req: HttpRequest, res: ClientHttpResponse ->
        if (BAD_REQUEST == res.statusCode && res.headers[DPOP_NONCE] != null) {
            runCatching {
                medNonce(request, req.uri, nonce(res))
            }.getOrElse {
                if (it is OAuth2AuthorizationException) throw it
                throw OAuth2AuthorizationException(
                    OAuth2Error(
                        INVALID_RESPONSE,
                        "Error response from token endpoint: ${res.statusCode} ${res.body}",
                        req.uri.toString()
                    ), it
                )
            }
        } else {
            throw OAuth2AuthorizationException(
                OAuth2Error(
                    INVALID_RESPONSE,
                    "Error response from first shot token endpoint: ${res.statusCode} ${res.body}",
                    req.uri.toString()
                )
            )
        }
    }

    private fun nonce(res: ClientHttpResponse) =
        res.headers[DPOP_NONCE]?.let {
            Nonce(it.single())
        } ?: throw OAuth2AuthorizationException(
            OAuth2Error(
                INVALID_RESPONSE,
                "No nonce in response from token endpoint",
                null
            )
        )

    private fun medNonce(request: RequestEntity<*>, uri: URI, nonce: Nonce) =
        with(request) {
            body?.let { b ->
                restClient.method(POST)
                    .uri(url)
                    .headers {
                        it.addAll(headers)
                        it.add(DPOP.value, generator.bevisFor(POST, uri, nonce = nonce))
                    }
                    .body(b)
                    .exchange(exchangeEtterNonce())
            }
        } ?: throw OAuth2AuthorizationException(OAuth2Error(INVALID_RESPONSE,
            "No body in request",
            request.url.toString())
        )

    private fun exchangeEtterNonce() = { req: HttpRequest, res: ClientHttpResponse ->
        if (!res.statusCode.is2xxSuccessful) {
            throw OAuth2AuthorizationException(
                OAuth2Error(
                    INVALID_RESPONSE,
                    "Unexpected response from token endpoint: ${res.statusCode} ${res.body}",
                    req.uri.toString()
                )
            )
        }
        runCatching {
            deserialize(res)
        }.getOrElse {
            throw OAuth2AuthorizationException(
                OAuth2Error(
                    INVALID_RESPONSE,
                    "Error response from token endpoint: ${res.statusCode}",
                    req.uri.toString()
                ), it
            )
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

        const val DPOP_NONCE = "dpop-nonce"
        const val INVALID_RESPONSE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DelegatingClientCredentialsTokenResponseClient::class.java)
    }
}

@Component
class DelegatingClientCredentialsTokenResponseClient(private val delegate: DPoPClientCredentialsTokenResponseClient) :
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        if (request.isDPoP()) {
            delegate.getTokenResponse(request)
        } else {
            DefaultClientCredentialsTokenResponseClient().getTokenResponse(request)
        }.also {
            log.info("Received token response for : ${request.clientRegistration.registrationId}")
        }

    companion object {
        private fun OAuth2ClientCredentialsGrantRequest.isDPoP() = clientRegistration.registrationId.startsWith(EDI20)
        const val INVALID_RESPONSE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DelegatingClientCredentialsTokenResponseClient::class.java)
    }
}
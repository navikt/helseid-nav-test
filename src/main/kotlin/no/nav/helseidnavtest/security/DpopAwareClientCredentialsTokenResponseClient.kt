package no.nav.helseidnavtest.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


class DpopAwareClientCredentialsTokenResponseClient(private val generator: DpopProofGenerator, val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {
    private val restOperations = RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter()))

    private val restClient = RestClient.builder().messageConverters {
        it.addAll(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter()))
    }.build()
    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest): OAuth2AccessTokenResponse? =
        requestEntityConverter.convert(request)?.let {
            log.info("Request converted to  ${it.body} ${it.headers} ${it.url}")
            getResponse(it)
        }

    private fun getResponse(request: RequestEntity<*>): OAuth2AccessTokenResponse? {
        try {
            log.info("Requesting token from ${request.url} med headers ${request.headers}")
            return restClient.method(POST)
                .uri(request.url)
                .headers {
                    it.addAll(request.headers)
                    it.add("dpop",generator.generateProof(POST, "${request.url}"))
                }.exchange { req, res ->
                    res.headers.forEach { (k, v) -> log.info("Response header $k: $v") }
                    res.bodyTo(OAuth2AccessTokenResponse::class.java)!!
                }
        } catch (e: RestClientException) {
            log.error("An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${e.message}",e)
            val oauth2Error = OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${e.message}", null)
            throw OAuth2AuthorizationException(oauth2Error, e)
        }
    }

    companion object {
        private const val INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response"
        private val log = LoggerFactory.getLogger(DpopAwareClientCredentialsTokenResponseClient::class.java)
    }
}

package no.nav.helseidnavtest.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

class DpopAwareClientCredentialsTokenResponseClient(private val generator: DpopProofGenerator, val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {
    private  val INVALID_TOKEN_RESPONSE_ERROR_CODE: String = "invalid_token_response"

    val restOperations = RestTemplate(listOf(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter()))

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest) =
        requestEntityConverter.convert(request)?.let {
            log.info("Request convertd to  ${it.body} ${it.headers} ${it.url}")
            getResponse(it).body }

    private fun getResponse(request: RequestEntity<*>): ResponseEntity<OAuth2AccessTokenResponse> {
        try {
            log.info("Requesting token from ${request.url}")
            return restOperations.exchange(request, OAuth2AccessTokenResponse::class.java).also {
                log.info("Received token response: ${it.body}")
            }
        } catch (e: RestClientException) {
            log.error("An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${e.message}",e)
            val oauth2Error = OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: ${e.message}", null)
            throw OAuth2AuthorizationException(oauth2Error, e)
        }
    }

    companion object{
        private val log = LoggerFactory.getLogger(DpopAwareClientCredentialsTokenResponseClient::class.java)

    }

}

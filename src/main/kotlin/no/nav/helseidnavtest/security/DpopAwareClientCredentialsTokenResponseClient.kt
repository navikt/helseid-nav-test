package no.nav.helseidnavtest.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.RequestEntity
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


class DpopAwareClientCredentialsTokenResponseClient(private val generator: DpopProofGenerator, val requestEntityConverter: Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<*>>) : OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {
    private  val INVALID_TOKEN_RESPONSE_ERROR_CODE: String = "invalid_token_response"
    var restOperations = RestTemplate(listOf<HttpMessageConverter<*>>(FormHttpMessageConverter(), OAuth2AccessTokenResponseHttpMessageConverter()))

    override fun getTokenResponse(request: OAuth2ClientCredentialsGrantRequest): OAuth2AccessTokenResponse? =
        requestEntityConverter.convert(request)?.let {
            log.info("Request converted to  ${it.body} ${it.headers} ${it.url}")
            getResponse(it)
        }

    private fun getResponse(request: RequestEntity<*>): OAuth2AccessTokenResponse? {
        try {
            log.info("Requesting token from ${request.url}")
            return restOperations.exchange<OAuth2AccessTokenResponse>(request).body
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

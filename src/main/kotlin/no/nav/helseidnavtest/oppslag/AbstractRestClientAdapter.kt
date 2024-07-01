package no.nav.helseidnavtest.oppslag

import com.nimbusds.oauth2.sdk.token.AccessTokenType
import com.nimbusds.oauth2.sdk.token.AccessTokenType.*
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.security.DpopEnabledClientCredentialsTokenResponseClient
import no.nav.helseidnavtest.security.DpopProofGenerator
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.web.client.RestClient
import java.util.*

abstract class AbstractRestClientAdapter(protected open val restClient : RestClient, protected val cfg : AbstractRestConfig,
                                         private val pingClient : RestClient = restClient) : Pingable {



    override fun ping() : Map<String, String> {
        if (isEnabled()) {
            pingClient
                .get()
                .uri(pingEndpoint())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful) { _, _ ->
                    log.trace("Ping ${pingEndpoint()} OK") }
            return emptyMap()
        }
        else return emptyMap()
    }

    override fun name() = cfg.name
    protected val baseUri = cfg.baseUri

    override fun pingEndpoint() = "${cfg.pingEndpoint}"
    override fun isEnabled() = cfg.isEnabled
    override fun toString() = "webClient=$restClient, cfg=$cfg, pingClient=$pingClient, baseUri=$baseUri"

    companion object {
        val log = getLogger(AbstractRestClientAdapter::class.java)

        private fun generellRequestInterceptor(key : String, value : () -> String) =
            ClientHttpRequestInterceptor { req, b, next ->
                req.headers.add(key, value())
                next.execute(req, b)
            }

        fun consumerRequestInterceptor() = generellRequestInterceptor(NAV_CONSUMER_ID) { HELSE }
        fun behandlingRequestInterceptor() = generellRequestInterceptor(BEHANDLINGSNUMMER) { BID }
        fun temaRequestInterceptor(tema : String) = generellRequestInterceptor(TEMA) { tema }


        private object CallIdGenerator {

            fun create() = "${UUID.randomUUID()}"
        }

        fun correlatingRequestInterceptor(defaultConsumerId : String) =
            ClientHttpRequestInterceptor { req, b, next ->

                with(req.headers) {
                    mapOf(
                        NAV_CONSUMER_ID to consumerId(defaultConsumerId),
                        NAV_CONSUMER_ID2 to consumerId(defaultConsumerId),
                        NAV_CALL_ID to callId(),
                        NAV_CALL_ID1 to callId(),
                        NAV_CALL_ID2 to callId(),
                        NAV_CALL_ID3 to callId()
                    ).forEach { (key, value) -> add(key, value) }
                }
                next.execute(req, b)
            }

        const val TEMA = "tema"
        const val HELSE = "helseopplysninger"
        private const val BEHANDLINGSNUMMER = "behandlingsnummer"
        private const val BID = "B287"
        private const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
        private const val NAV_CONSUMER_ID2 = "consumerId"
        private const val NAV_CALL_ID = "Nav-CallId"
        private const val NAV_CALL_ID1 = "Nav-Call-Id"
        private const val NAV_CALL_ID2 = "callId"
        private const val NAV_CALL_ID3 = "X-Correlation-ID"

        private fun callId() = MDC.get(NAV_CALL_ID) ?: run {
            val id = CallIdGenerator.create()
            toMDC(NAV_CALL_ID, id)
            id
        }

        private fun consumerId(defaultValue : String) : String = MDC.get(NAV_CONSUMER_ID) ?: run {
            toMDC(NAV_CONSUMER_ID, defaultValue)
            defaultValue
        }

        private fun toMDC(key : String, value : String?, defaultValue : String? = null) = MDC.put(key, value ?: defaultValue)
    }
}

class TokenExchangingRequestInterceptor(
    private val proofGenerator: DpopProofGenerator,
    private val shortName: String,
    private val clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager,
    private val tokenType: AccessTokenType = BEARER
) : ClientHttpRequestInterceptor {
    val log = getLogger(TokenExchangingRequestInterceptor::class.java)

    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
       log.info("Token exchange for {}", shortName)
        clientManager.authorize(
            withClientRegistrationId(shortName)
                .principal("dpop or whatever")
                .build()
        )?.let { c ->
            if (tokenType == DPOP)
             proofGenerator.generate(req.method, req.uri.toString()).also {
                log.info("DPoP proof: {}", it)
                req.headers.set("DPoP", it)
            }
            req.headers.set(AUTHORIZATION,tokenType.value + " " + c.accessToken.tokenValue)
            .also {
                log.info("Token {} exchanged for {}", c.accessToken.tokenValue,c.clientRegistration.registrationId)
            }
        } ?: log.error("No Authorized client for {}", shortName)
        return execution.execute(req, body)
    }

    override fun toString() = "TokenExchangingRequestInterceptor(shortName=$shortName)"
}

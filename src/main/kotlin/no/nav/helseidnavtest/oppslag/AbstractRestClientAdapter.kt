package no.nav.helseidnavtest.oppslag

import com.nimbusds.oauth2.sdk.token.AccessTokenType
import com.nimbusds.oauth2.sdk.token.AccessTokenType.*
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.HERID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MOTTAGER
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.SENDER
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.security.DPoPBevisGenerator
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
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

open class TokenExchangingRequestInterceptor(
    private val clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager,
    defaultShortName: String?,
    private val tokenType: AccessTokenType = BEARER) : ClientHttpRequestInterceptor {
    private val resolver = HerIdToShortNameMapper(defaultShortName)
    protected val log = getLogger(TokenExchangingRequestInterceptor::class.java)

    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution) =
        with(req) {
            authorize(this)
            execution.execute(this, body)
        }

    protected fun authorize(req: HttpRequest) =
        clientManager.authorize(
            withClientRegistrationId(resolver.resolve(req))
                .principal("whatever")
                .build()
        )?.apply {  ->
            req.headers.set(AUTHORIZATION, "${tokenType.value} ${accessToken.tokenValue}")
                .also {
                    log.info("Token {} exchanged for {}", accessToken.tokenValue,clientRegistration.registrationId)
                }
        }
    private class HerIdToShortNameMapper(private val defaultShortName: String?)  {
        fun resolve(req : HttpRequest) = defaultShortName ?: shortNameFromHeader(req)

        private fun shortNameFromHeader(req: HttpRequest) =
            when (val herId = req.headers[HERID]?.single()) {
                SENDER.first   -> SENDER.second
                MOTTAGER.first -> MOTTAGER.second
                null -> throw IllegalArgumentException("No herId in request header")
                else -> throw IllegalArgumentException("Unknown herId  $herId in request header")
            }
    }
}
class DPoPEnabledTokenExchangingRequestInterceptor(private val generator: DPoPBevisGenerator, shortName: String,
                                                   clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) : TokenExchangingRequestInterceptor(clientManager, shortName, DPOP) {
    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution) =
        with(req){
            authorize(this)?.let { client ->
                generator.bevisFor(method, uri, client.accessToken).also {
                    headers.set(DPOP.value, it)
                }
            }
            execution.execute(this, body)
        }
}


package no.nav.helseidnavtest.oppslag

import no.nav.helseidnavtest.health.Pingable
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.client.ClientHttpRequestInterceptor
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
                req.headers.add(key, value.invoke())
                next.execute(req, b)
            }

        fun consumerRequestInterceptor() = generellRequestInterceptor(NAV_CONSUMER_ID) { "helse" }
        fun behandlingRequestInterceptor() = generellRequestInterceptor(BEHANDLINGSNUMMER) { BID }
        fun temaRequestInterceptor(tema : String) = generellRequestInterceptor(TEMA) { tema }


        private object CallIdGenerator {

            fun create() = "${UUID.randomUUID()}"
        }        /*
        fun correlatingFilterFunction(defaultConsumerId : String) =
            ExchangeFilterFunction { req : ClientRequest, next : ExchangeFunction ->
                next.exchange(
                    ClientRequest.from(req)
                        .header(NAV_CONSUMER_ID, consumerId(defaultConsumerId))
                        .header(NAV_CONSUMER_ID2, consumerId(defaultConsumerId))
                        .header(NAV_CALL_ID, callId())
                        .header(NAV_CALL_ID1, callId())
                        .header(NAV_CALL_ID2, callId())
                        .header(NAV_CALL_ID3, callId())
                        .build())
            }

*/
        const val TEMA = "tema"
        const val HELSE = "helseopplysninger"
        const val BEHANDLINGSNUMMER = "behandlingsnummer"
        const val BID = "B287"
        const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
        const val NAV_CONSUMER_ID2 = "consumerId"
        const val NAV_CALL_ID = "Nav-CallId"
        const val NAV_CALL_ID1 = "Nav-Call-Id"
        const val NAV_CALL_ID2 = "callId"
        const val NAV_CALL_ID3 = "X-Correlation-ID"

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


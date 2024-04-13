package no.nav.helseidnavtest.oppslag.rest

import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

abstract class AbstractWebClientAdapter(protected open val webClient : WebClient, protected val cfg : AbstractRestConfig,
                                        private val pingClient : WebClient = webClient) : Pingable {

    override fun ping() : Map<String, String> {
        if (isEnabled()) {
            pingClient
                .get()
                .uri(pingEndpoint())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess { log.trace("Ping ${pingEndpoint()} OK") }
                .doOnError { t : Throwable -> log.warn("Ping feilet", t) }
                .contextCapture()
                .block()
            return emptyMap()
        }
        else return emptyMap()
    }

    override fun name() = cfg.name
    protected val baseUri = cfg.baseUri

   // protected fun retrySpec(log: Logger, path: String, filter: Predicate<Throwable>) = cfg.retrySpec(log,path,filter)
    override fun pingEndpoint() = "${cfg.pingEndpoint}"
    override fun isEnabled() = cfg.isEnabled
    override fun toString() = "webClient=$webClient, cfg=$cfg, pingClient=$pingClient, baseUri=$baseUri"

    companion object {

        @JvmStatic
        protected val log : Logger = getLogger(AbstractWebClientAdapter::class.java)
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

        fun generellFilterFunction(key : String, value : () -> String) =
            ExchangeFilterFunction { req : ClientRequest, next : ExchangeFunction ->
                next.exchange(
                    ClientRequest.from(req)
                        .header(key, value.invoke())
                        .build())
            }

        fun temaFilterFunction() = generellFilterFunction(TEMA) { HELSE }
        fun behandlingFilterFunction() = generellFilterFunction(BEHANDLINGSNUMMER) { BID }

        const val NAV_PERSON_IDENT = "Nav-Personident"
        const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
        const val NAV_CONSUMER_ID2 = "consumerId"
        const val NAV_CALL_ID = "Nav-CallId"
        const val NAV_CALL_ID1 = "Nav-Call-Id"
        const val NAV_CALL_ID2 = "callId"
        const val NAV_CALL_ID3 = "X-Correlation-ID"

        fun callId() = MDC.get(NAV_CALL_ID) ?: run {
            val id = CallIdGenerator.create()
            toMDC(NAV_CALL_ID, id)
            id
        }

        fun consumerId(defaultValue : String) : String = MDC.get(NAV_CONSUMER_ID) ?: run {
            toMDC(NAV_CONSUMER_ID, defaultValue)
            defaultValue
        }

        private fun toMDC(key : String, value : String?, defaultValue : String? = null) = MDC.put(key, value ?: defaultValue)

        const val TEMA = "tema"
        const val HELSE = "helseopplysninger"
        const val BEHANDLINGSNUMMER = "behandlingsnummer"
        const val BID = "B287"
        }
}
object CallIdGenerator {

    fun create() = "${UUID.randomUUID()}"
}
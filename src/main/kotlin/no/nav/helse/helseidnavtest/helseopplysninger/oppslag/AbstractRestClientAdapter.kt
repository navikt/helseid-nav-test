package no.nav.helse.helseidnavtest.helseopplysninger.oppslag

import no.nav.helse.helseidnavtest.helseopplysninger.health.Pingable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClient

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

        @JvmStatic
        protected val log = getLogger(AbstractRestClientAdapter::class.java)
        /*
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

         fun consumerFilterFunction() = generellFilterFunction(NAV_CONSUMER_ID) { AAP }
         fun temaFilterFunction() = generellFilterFunction(TEMA) { AAP }
         fun behandlingFilterFunction() = generellFilterFunction(BEHANDLINGSNUMMER) { BID }
         */
     }

}
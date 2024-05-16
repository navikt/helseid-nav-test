package no.nav.helseidnavtest.oppslag

import no.nav.helseidnavtest.health.Pingable
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.client.RestClient

abstract class AbstractRestClientAdapter(protected open val restClient : RestClient, protected val cfg : AbstractRestConfig,
                                         private val pingClient : RestClient = restClient) : Pingable {

    protected val log = getLogger(AbstractRestClientAdapter::class.java)


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

}
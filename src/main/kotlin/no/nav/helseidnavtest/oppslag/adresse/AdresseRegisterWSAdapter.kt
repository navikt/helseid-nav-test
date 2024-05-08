package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyService
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component

@Component
class AdresseRegisterWSAdapter(private val cfg: AdresseRegisterConfig) : Pingable {

    private val log = getLogger(AdresseRegisterWSAdapter::class.java)

    private val client = createPort<ICommunicationPartyService>("${cfg.url}") {
        proxy {}
        port {
            if (cfg.username != null && cfg.password != null) {
                withBasicAuth(cfg.username, cfg.password)
            }
        }
    }

    fun herIdForId(id: String): Int = runCatching {
        client.searchById(id).communicationParty.single().herId.also {
            log.info("Returnerer kommunikasjonspart $it ")
        }
    }.getOrElse {
            when (it) {
                is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Ukjent herId", it.message ?: "Fant ikke noe for $id", cfg.url,it)
                is NoSuchElementException -> throw NotFoundException("Ukjent herId", "Fant ikke noe for $id", cfg.url,it)
                is IllegalStateException -> throw it
                else -> throw RecoverableException(BAD_REQUEST, "${it.message}", cfg.url, it)
            }
        }

    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = "${cfg.url}"

}

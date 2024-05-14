package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nhn.register.communicationparty.ICommunicationPartyService
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component

@Component
class AdresseRegisterWSAdapter(private val cfg: AdresseRegisterConfig) : Pingable {

    private val log = getLogger(AdresseRegisterWSAdapter::class.java)

    private val client = createPort<ICommunicationPartyService>("${cfg.url}") {
        proxy {}
        port {
            withBasicAuth(cfg.username, cfg.password)
        }
    }

    fun herIdForId(id: String): Int = runCatching {
        client.searchById(id).communicationParty.single().herId.also {
            log.info("Returnerer kommunikasjonspart $it for $id")
        }
    }.getOrElse {
            when (it) {
                is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Feil ved oppslag av $id", it.message, cfg.url,it)
                is NoSuchElementException -> throw NotFoundException(detail = "Fant ikke kommunikasjonspart for $id", uri = cfg.url, cause = it)
                is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR, "For mange kommunikasjonsparter for $id", it.message,cfg.url,it)
                else -> throw RecoverableException(BAD_REQUEST, it.message ?: "", cfg.url, it)
            }
        }

    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = "${cfg.url}"

}

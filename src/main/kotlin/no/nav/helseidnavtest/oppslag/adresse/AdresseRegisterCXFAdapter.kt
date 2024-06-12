package no.nav.helseidnavtest.oppslag.adresse
import jakarta.xml.ws.BindingProvider
import jakarta.xml.ws.BindingProvider.*
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nhn.register.communicationparty.CommunicationParty_Service
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component

@Component
class AdresseRegisterCXFAdapter(private val cfg: AdresseRegisterConfig) : Pingable {


    private val log = getLogger(AdresseRegisterCXFAdapter::class.java)

    private val client = CommunicationParty_Service().wsHttpBindingICommunicationPartyService
   init {
       (client as BindingProvider).requestContext.apply {
           put(USERNAME_PROPERTY, cfg.username)
           put(PASSWORD_PROPERTY, cfg.password)
           put(ENDPOINT_ADDRESS_PROPERTY, "${cfg.url}")
       }
   }

    fun herIdForId(id: String): Int = runCatching {
        client.searchById(id).communicationParty.single().herId.also {
            log.info("Returnerer kommunikasjonspart $it for $id")
        }
    }.getOrElse {
            when (it) {
                is CommPartyFault -> throw NotFoundException("Feil ved oppslag av $id", it.message, cfg.url,it)
                is NoSuchElementException -> throw NotFoundException(detail = "Fant ikke kommunikasjonspart for $id", uri = cfg.url, cause = it)
                is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR, "For mange kommunikasjonsparter for $id", it.message,cfg.url,it)
                else -> throw RecoverableException(BAD_REQUEST, it.message ?: "", cfg.url, it)
            }
        }

    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = "${cfg.url}"

}

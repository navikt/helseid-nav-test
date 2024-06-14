package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nhn.register.communicationparty.CommunicationParty_Service
import no.nhn.register.communicationparty.ICommunicationPartyService
import org.apache.cxf.configuration.security.AuthorizationPolicy
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPConduit
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component

import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault


@Component
class AdresseRegisterCXFAdapter(private val cfg: AdresseRegisterConfig) : Pingable {


    private val log = getLogger(AdresseRegisterCXFAdapter::class.java)


    private val client = service(cfg)

    private fun init(service: CommunicationParty_Service): ICommunicationPartyService {

        (ClientProxy.getClient(service.getPort(ICommunicationPartyService::class.java)).conduit as HTTPConduit).authorization.apply {
            userName = cfg.username;
            password = cfg.password
        }
        return service.wsHttpBindingICommunicationPartyService
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

    fun service(cfg: AdresseRegisterConfig): ICommunicationPartyService {


        try {


        val factory = JaxWsProxyFactoryBean().apply {
            address = cfg.url.toString()
        }

        log.info("Creating service for ${cfg.url}")

        val service = factory.create(ICommunicationPartyService::class.java)

        log.info("Service created for ${cfg.url}")
        val client: Client = ClientProxy.getClient(service)

        log.info("Client created for ${cfg.url}")

        // Add logging interceptors for debugging
        client.inInterceptors.add(LoggingInInterceptor())
        client.outInterceptors.add(LoggingOutInterceptor())

        // Set up HTTP authentication
        val httpConduit = client.conduit as HTTPConduit
        val authorizationPolicy = AuthorizationPolicy().apply {
            userName = cfg.username
            password = cfg.password
        }
        httpConduit.authorization = authorizationPolicy
        log.info("HTTP Conduit created for ${cfg.url}")
        return service
        } catch (e: Exception) {
            log.error("Error creating service for ${cfg.url}", e)
            throw e
        }
    }

}




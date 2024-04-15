package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyService
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component

@Component
class AdresseWSAdapter(private val cfg: AdresseConfig) : Pingable {

    private val client = createPort<ICommunicationPartyService>("${cfg.url}") {
        proxy {}
        port { withBasicAuth(cfg.username, cfg.password) }
    }
    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = "${cfg.url}"

    fun details(herId: Int) =
        runCatching {
            client.getCommunicationPartyDetails(herId)
        }.fold(
            onSuccess = { it },
            onFailure = {
                when (it) {
                    is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw NotFoundException(it.message ?: "Fant ikke noe for herId=$herId", cfg.url)
                    else -> throw RecoverableException("${it.message}", cfg.url, BAD_REQUEST,it)
                }
            }
        )
}

//       herId 83849


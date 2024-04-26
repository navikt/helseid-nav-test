package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyService
import no.nav.helseidnavtest.ws.ar.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage
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

    fun herIdForHpr(hpr: Int) = searchById(hpr.toString())

    fun herIdForKontor(kontor: Virksomhetsnummer) = searchById(kontor.value)

    private fun searchById(id: String) = runCatching {
        client.searchById(id).communicationParty.first().herId
    }.fold(
        onSuccess = { it },
        onFailure = {
            when (it) {
                is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw NotFoundException(
                    "Ukjent herId",
                    it.message ?: "Fant ikke noe for $id",
                    cfg.url
                )
                else -> throw RecoverableException(BAD_REQUEST, "${it.message}", cfg.url, it)
            }
        }
    )
}

//       herId 83849


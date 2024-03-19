package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.adresse


import no.nav.helse.helseidnavtest.helseopplysninger.error.IntegrationException
import no.nav.helse.helseidnavtest.helseopplysninger.error.OppslagNotFoundException
import no.nav.helse.helseidnavtest.helseopplysninger.health.Pingable
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.createPort
import no.nav.helse.helseidnavtest.ws.ar.ICommunicationPartyService
import no.nav.helse.helseidnavtest.ws.ar.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage

@Component
class AdresseWSAdapter(private val cfg: AdresseConfig) : Pingable {

    private val client = createPort<ICommunicationPartyService>(cfg.url) {
        proxy {}
        port { withBasicAuth(cfg.username, cfg.password) }
    }
    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = cfg.url

    fun details(herId: Int) =
        runCatching {
            client.getCommunicationPartyDetails(herId)
        }.fold(
            onSuccess = { it },
            onFailure = {
                when (it) {
                    is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw OppslagNotFoundException(it.message ?: "Fant ikke noe for herId=$herId")
                    else -> throw IntegrationException("${it.message}", it)
                }
            }
        )
}

//       herId 83849


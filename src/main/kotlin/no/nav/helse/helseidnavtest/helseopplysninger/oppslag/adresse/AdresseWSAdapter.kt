package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.adresse


import no.nav.helse.helseidnavtest.helseopplysninger.health.Pingable
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.createPort
import no.nav.helse.helseidnavtest.ws.ar.ICommunicationPartyService
@Component
class AdresseWSAdapter(private val cfg: AdresseConfig) : Pingable {

    private val client = createPort<ICommunicationPartyService>(cfg.url) {
        proxy {}
        port { withBasicAuth(cfg.username, cfg.password) }
    }
    override fun ping() = mapOf(Pair("ping",client.ping()))
    override fun pingEndpoint() = cfg.url

    fun details(herId: Int) = client.getCommunicationPartyDetails(herId)
}
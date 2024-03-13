package no.nav.helse.helseidnavtest.helseopplysninger.adresse


import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.createPort
import no.nav.helse.helseidnavtest.ws.ar.ICommunicationPartyService
@Component
class AdresseWSAdapter(cfg: AdresseConfig) {

    private val client = createPort<ICommunicationPartyService>(cfg.url) {
        proxy {}
        port { withBasicAuth(cfg.username, cfg.password) }
    }
    fun ping() = client.ping()
}
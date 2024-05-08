package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.emottak.HentPartnerIDViaOrgnummerRequest
import no.nav.helseidnavtest.ws.emottak.PartnerResource
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class DialogmeldingWSAdapter(cfg: DialogmeldingConfig) : Pingable {

    private val log = getLogger(DialogmeldingWSAdapter::class.java)


    private val client = createPort<PartnerResource>("${cfg.baseUri}") {
       // proxy {}
        port {}
    }

    fun partnerRef(orgnr: String, herId: String) = client.hentPartnerIDViaOrgnummer(HentPartnerIDViaOrgnummerRequest().apply {
        setOrgnr(orgnr)
    }).partnerInformasjon.forEach { log.info("${it.partnerID} for ${it.heRid}") }


    override fun ping(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun pingEndpoint(): String {
        TODO("Not yet implemented")
    }


}

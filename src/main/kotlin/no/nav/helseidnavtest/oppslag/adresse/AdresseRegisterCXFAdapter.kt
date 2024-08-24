package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nav.helseidnavtest.oppslag.CXFErrorHandler
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.*
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
import org.springframework.stereotype.Component
import no.nhn.register.communicationparty.Organization as KommunikasjonsPartVirksomhet
import no.nhn.register.communicationparty.OrganizationPerson as KommunikasjonsPartPerson
import no.nhn.register.communicationparty.Service as KommunikasjonsPartTjeneste

@Component
class AdresseRegisterCXFAdapter(cfg: AdresseRegisterConfig, private val handler: CXFErrorHandler) :
    AbstractCXFAdapter(cfg) {

    private val client = client<ICommunicationPartyService>()
    private val mapper = KommunikasjonsPartMapper(client)

    fun kommunikasjonsPart(herId: Int) =
        runCatching {
            client.getCommunicationPartyDetails(herId).let { mapper.map(it, herId.herId()) }
        }.getOrElse {
            handler.handleError(it, herId.herId())
        }

    override fun ping() = mapOf(Pair("ping", client.ping()))
    private fun Int.herId() = HerId(this)

}

private class KommunikasjonsPartMapper(private val client: ICommunicationPartyService) {
    fun map(it: CommunicationParty, herId: HerId) =
        when (it) {
            is KommunikasjonsPartVirksomhet -> Virksomhet(it)
            is KommunikasjonsPartPerson -> VirksomhetPerson(it, client.getOrganizationDetails(it.parentHerId))
            is KommunikasjonsPartTjeneste -> Tjeneste(it, client.getOrganizationDetails(it.parentHerId))
            else -> throw IllegalStateException("Ukjent type kommunikasjonspartfor herId $herId ${it.javaClass.simpleName}")
        }
}








package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
import no.nhn.register.communicationparty.OrganizationPerson
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault
import no.nhn.register.communicationparty.Organization as KommunikasjonsPartVirksomhet
import no.nhn.register.communicationparty.OrganizationPerson as KommunikasjonsPartPerson
import no.nhn.register.communicationparty.Service as KommunikasjonsPartTjeneste

@Component
class AdresseRegisterCXFAdapter(cfg: AdresseRegisterConfig) : AbstractCXFAdapter(cfg) {

    private val client = client<ICommunicationPartyService>()

    private fun virksomhet(id: Int) = client.getOrganizationDetails(id)

    fun kommunikasjonsPart(id: String): KommunikasjonsPart =
        runCatching {
            client.getCommunicationPartyDetails(id.toInt()).also {
                log.info("Hentet kommunikasjonspart for $id fra ${cfg.url} med navn ${it.name.value} og type ${it.type.single()}")
            }.let {
                log.info("Mapper kommunikasjonspart for $id")
                when (it) {
                    is KommunikasjonsPartVirksomhet -> Virksomhet(it)
                    is KommunikasjonsPartPerson -> VirksomhetPerson(it)
                    is KommunikasjonsPartTjeneste -> Tjeneste(it, virksomhet(it.parentHerId))
                    else -> throw IllegalStateException("Ukjent type kommunikasjonspart ${it.javaClass}")
                }
            }.also { log.info("Kommunikasjonspart etter mapping for $id er $it") }
        }.getOrElse {
            when (it) {
                is CommPartyFault -> throw NotFoundException(it.message, cfg.url, cause = it)
                is IllegalArgumentException -> throw IrrecoverableException(BAD_REQUEST,
                    cfg.url,
                    it.message,
                    cause = it)

                is NoSuchElementException -> throw NotFoundException("Fant ikke kommunikasjonspart for $id",
                    cfg.url,
                    cause = it)

                is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,
                    cfg.url,
                    it.message,
                    cause = it)

                else -> throw RecoverableException(BAD_REQUEST, cfg.url, it.message, it).also {
                    log.warn(it.message, it)
                }
            }
        }

    override fun ping() = mapOf(Pair("ping", client.ping()))

}

data class Bestilling(val tjenester: Tjenester, val pasient: Pasient)

open class KommunikasjonsPart(aktiv: Boolean, val visningsNavn: String?, val herId: HerId, val navn: String) {

    init {
        require(aktiv) { "Kommunikasjonspart er ikke aktiv" }
    }

    constructor(p: CommunicationParty) : this(p.isActive, p.displayName.value, p.herId(), p.name.value)
}

class Virksomhet(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String) :
    KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
    constructor(v: KommunikasjonsPartVirksomhet) : this(v.isActive, v.displayName.value, v.herId(), v.name.value)
}

data class VirksomhetPerson(val person: OrganizationPerson) : KommunikasjonsPart(person)

class Tjeneste(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String, val virksomhet: Virksomhet) :
    KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
    constructor(t: KommunikasjonsPartTjeneste, virksomhet: KommunikasjonsPartVirksomhet) :
            this(t.isActive, t.displayName.value, t.herId(), t.name.value, Virksomhet(virksomhet))
}

enum class Type { Organization, Person, Service }

private fun CommunicationParty.herId() = herId.herId()

private fun Int.herId() = HerId(this)

data class Tjenester(val fra: Tjeneste, val til: Tjeneste)







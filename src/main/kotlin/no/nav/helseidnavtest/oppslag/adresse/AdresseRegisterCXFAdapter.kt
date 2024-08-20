package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nav.helseidnavtest.oppslag.adresse.Type.*
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
import no.nhn.register.communicationparty.OrganizationPerson
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault
import no.nhn.register.communicationparty.Organization as Organisasjon

@Component
class AdresseRegisterCXFAdapter(cfg: AdresseRegisterConfig) : AbstractCXFAdapter(cfg) {

    private val client = client<ICommunicationPartyService>()

    fun tjeneste(id: String) = client.getServiceDetails(id.toInt())
    fun virksomhet(id: String) = client.getOrganizationDetails(id.toInt())
    fun person(id: String) = client.getOrganizationPersonDetails(id.toInt())

    fun kommunikasjonsPart(id: String): KommunikasjonsPart =
        runCatching {
            client.getCommunicationPartyDetails(id.toInt()).also {
                log.info("Hentet kommunikasjonspart for $id fra ${cfg.url} med navn ${it.name.value} og type ${it.type.single()}")
            }.let {
                log.info("Mapper kommunikasjonspart for $id")
                when (Type.valueOf(it.type.single())) {
                    Organization -> Virksomhet(it as Organisasjon)
                    Person -> VirksomhetPerson(it as OrganizationPerson)
                    Service -> Tjeneste(it as no.nhn.register.communicationparty.Service,
                        virksomhet(it.parentHerId.toString()))
                }
            }.also { log.info("Kommunikasjonspart etter mapping for $id er $it") }
        }.getOrElse { e ->
            when (e) {
                is CommPartyFault -> throw IrrecoverableException.NotFoundException(e.message, cfg.url, cause = e)
                is IllegalArgumentException -> throw IrrecoverableException(BAD_REQUEST,
                    cfg.url,
                    e.message,
                    cause = e)

                is NoSuchElementException -> throw IrrecoverableException.NotFoundException("Fant ikke kommunikasjonspart for $id",
                    cfg.url,
                    cause = e)

                is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,
                    cfg.url,
                    e.message,
                    cause = e)

                else -> throw RecoverableException(BAD_REQUEST, cfg.url, e.message, e).also {
                    log.warn(e.message, e)
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

    constructor(part: CommunicationParty) : this(part.isActive,
        part.displayName.value,
        part.herId.herId(),
        part.name.value)
}

class Virksomhet(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String) :
    KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
    constructor(virksomhet: Organisasjon) : this(virksomhet.isActive,
        virksomhet.displayName.value,
        virksomhet.herId.herId(),
        virksomhet.name.value)
}

data class VirksomhetPerson(val person: OrganizationPerson) : KommunikasjonsPart(person)

class Tjeneste(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String, val virksomhet: Virksomhet) :
    KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
    constructor(tjeneste: no.nhn.register.communicationparty.Service,
                virksomhet: Organisasjon) : this(tjeneste.isActive,
        tjeneste.displayName.value,
        tjeneste.herId.herId(),
        tjeneste.name.value, Virksomhet(virksomhet))
}

enum class Type {
    Organization, Person, Service
}

private fun Int.herId() = HerId(this)

data class Tjenester(val fra: Tjeneste, val til: Tjeneste)







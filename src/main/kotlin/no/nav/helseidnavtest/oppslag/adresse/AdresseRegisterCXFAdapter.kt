package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.*
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
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

    fun kommunikasjonsPart(id: String) =
        runCatching {
            client.getCommunicationPartyDetails(id.toInt()).also {
                log.info("Hentet kommunikasjonspart for $id fra ${cfg.url} med navn ${it.name.value} og type ${it.type.single()}")
            }.let {
                log.info("Mapper kommunikasjonspart for $id")
                when (it) {
                    is KommunikasjonsPartVirksomhet -> Virksomhet(it)
                    is KommunikasjonsPartPerson -> VirksomhetPerson(it, client.getOrganizationDetails(it.parentHerId))
                    is KommunikasjonsPartTjeneste -> Tjeneste(it, client.getOrganizationDetails(it.parentHerId))
                    else -> throw IllegalStateException("Ukjent type kommunikasjonspart ${it.javaClass}")
                }
            }
        }.getOrElse {
            handleError(it, id)
        }

    private fun handleError(it: Throwable, id: String): Nothing = when (it) {
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

    override fun ping() = mapOf(Pair("ping", client.ping()))

}

data class Bestilling(val tjenester: Tjenester, val pasient: Pasient) {
    data class Tjenester(val fra: Tjeneste, val til: Tjeneste)
}

abstract class KommunikasjonsPart(aktiv: Boolean, val visningsNavn: String?, val herId: HerId, val navn: String) {

    enum class Type { Organization, Person, Service }

    init {
        require(aktiv) { "Kommunikasjonspart er ikke aktiv" }
    }

    class Virksomhet(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(virksomhet: KommunikasjonsPartVirksomhet) : this(virksomhet.isActive,
            virksomhet.displayName.value,
            virksomhet.herId(),
            virksomhet.name.value)
    }

    class VirksomhetPerson(aktiv: Boolean,
                           visningsNavn: String?,
                           herId: HerId,
                           navn: String,
                           val virksomhet: Virksomhet) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(person: KommunikasjonsPartPerson, virksomhet: KommunikasjonsPartVirksomhet) : this(person.isActive,
            person.displayName.value,
            person.herId(),
            person.name.value, Virksomhet(virksomhet))
    }

    class Tjeneste(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String, val virksomhet: Virksomhet) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(tjeneste: KommunikasjonsPartTjeneste, virksomhet: KommunikasjonsPartVirksomhet) :
                this(tjeneste.isActive,
                    tjeneste.displayName.value,
                    tjeneste.herId(),
                    tjeneste.name.value,
                    Virksomhet(virksomhet))
    }
}

fun CommunicationParty.herId() = HerId(herId)








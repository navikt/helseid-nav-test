package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.HerId.Companion.NONE
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nav.helseidnavtest.oppslag.adresse.Type.*
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault

@Component
class AdresseRegisterCXFAdapter(cfg: AdresseRegisterConfig) : AbstractCXFAdapter(cfg) {

    private val client = client<ICommunicationPartyService>()

    fun kommunikasjonsPart(id: String) =
        runCatching {
            client.getCommunicationPartyDetails(id.toInt()).also {
                log.info("Hentet kommunikasjonspart for $id fra ${cfg.url} med navn ${it.name.value} og type ${it.type.single()}")
            }.let {
                when (Type.valueOf(it.type.single())) {
                    Organization -> Virksomhet(it)
                    Person -> VirksomhetPerson(it)
                    Service -> Tjeneste(it)
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

                else -> throw RecoverableException(BAD_REQUEST, cfg.url, it.message, it)
            }
        }

    override fun ping() = mapOf(Pair("ping", client.ping()))

}

data class Bestilling(val parter: KommunikasjonsParter, val pasient: Pasient)

open class KommunikasjonsPart(val aktiv: Boolean,
                              val visningsNavn: String,
                              val herId: HerId,
                              val navn: String,
                              val parentHerId: HerId,
                              val parentNavn: String) {

    constructor(party: CommunicationParty) : this(
        aktiv = party.isActive,
        visningsNavn = party.displayName.value,
        herId = HerId(party.herId),
        navn = party.name.value,
        parentHerId = party.parentHerId.takeIf { it.toInt() > 0 }?.let { HerId(it) } ?: NONE,
        parentNavn = party.parentName.value)
}

data class Virksomhet(val party: CommunicationParty) : KommunikasjonsPart(party)

data class VirksomhetPerson(val party: CommunicationParty) : KommunikasjonsPart(party)

data class Tjeneste(val party: CommunicationParty) : KommunikasjonsPart(party)

enum class Type {
    Organization, Person, Service
}

data class KommunikasjonsParter(val fra: KommunikasjonsPart, val til: KommunikasjonsPart)







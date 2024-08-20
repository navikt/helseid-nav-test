package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
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
                log.info("Mapper kommunikasjonspart for $id")
                when (Type.valueOf(it.type.single())) {
                    Organization -> Virksomhet(it)
                    Person -> VirksomhetPerson(it)
                    Service -> Tjeneste(it)
                }
            }.also { log.info("Kommunikasjonspart etter mapping for $id er $it") }
        }.getOrElse { e ->
            when (e) {
                is CommPartyFault -> throw NotFoundException(e.message, cfg.url, cause = e)
                is IllegalArgumentException -> throw IrrecoverableException(BAD_REQUEST,
                    cfg.url,
                    e.message,
                    cause = e)

                is NoSuchElementException -> throw NotFoundException("Fant ikke kommunikasjonspart for $id",
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

data class Bestilling(val parter: KommunikasjonsParter, val pasient: Pasient)

open class KommunikasjonsPart(val aktiv: Boolean,
                              val visningsNavn: String?,
                              val herId: HerId,
                              val navn: String,
                              val virksomhet: KommunikasjonsPart? = null) {

    init {
        require(aktiv) { "Kommunikasjonspart er ikke aktiv" }
    }

    constructor(tjeneste: CommunicationParty, virksomhet: CommunicationParty? = null) : this(
        aktiv = tjeneste.isActive,
        visningsNavn = tjeneste.displayName.value,
        herId = HerId(tjeneste.herId),
        navn = tjeneste.name.value,
        virksomhet = virksomhet?.let { KommunikasjonsPart(it) })
}

data class Virksomhet(val party: CommunicationParty) : KommunikasjonsPart(party)

data class VirksomhetPerson(val party: CommunicationParty) : KommunikasjonsPart(party)

data class Tjeneste(val party: CommunicationParty) : KommunikasjonsPart(party)

enum class Type {
    Organization, Person, Service
}

data class KommunikasjonsParter(val fra: KommunikasjonsPart, val til: KommunikasjonsPart)







package no.nav.helseidnavtest.oppslag.adresse

import net.minidev.json.annotate.JsonIgnore
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Mottaker
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.Organization
import no.nhn.register.communicationparty.OrganizationPerson
import no.nhn.register.communicationparty.Service
import java.net.URI
import java.util.*

abstract class KommunikasjonsPart(val aktiv: Boolean = true,
                                  visningsNavn: String?,
                                  val herId: HerId,
                                  val navn: String) {

    enum class Type { Organization, Person, Service }

    val orgNavn = visningsNavn ?: navn

    init {
        require(aktiv) { "Kommunikasjonspart ${herId.verdi} (${navn} er ikke aktiv" }
    }

    class Virksomhet(aktiv: Boolean,
                     visningsNavn: String?,
                     herId: HerId,
                     navn: String) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(virksomhet: Organization) :
                this(virksomhet.isActive,
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
        constructor(person: OrganizationPerson, virksomhet: Organization) :
                this(person.isActive,
                    person.displayName.value,
                    person.herId(),
                    person.name.value,
                    Virksomhet(virksomhet))
    }

    class Tjeneste(aktiv: Boolean,
                   visningsNavn: String?,
                   herId: HerId,
                   navn: String,
                   val virksomhet: Virksomhet) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(tjeneste: Service, virksomhet: Organization) :
                this(tjeneste.isActive,
                    tjeneste.displayName.value,
                    tjeneste.herId(),
                    tjeneste.name.value,
                    Virksomhet(virksomhet))
    }

    data class Mottaker(val part: KommunikasjonsPart, val navn: Navn)
}

data class Innsending(val id: UUID,
                      val parter: Parter,
                      val pasient: Pasient,
                      val vedlegg: ByteArray? = null,
                      val ref: Pair<URI, String>? = null) {

    @JsonIgnore
    val avsender = parter.avsender.herId

    data class Parter(val avsender: KommunikasjonsPart, val mottaker: Mottaker)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Innsending

        return id == other.id
    }

    override fun hashCode() = id.hashCode()
    override fun toString() =
        javaClass.simpleName + "(id=$id, parter=$parter, pasient=$pasient, vedlegg=${vedlegg?.size}, ref=$ref)"
}

private fun CommunicationParty.herId() = HerId(herId)

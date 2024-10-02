package no.nav.helseidnavtest.oppslag.adresse

import net.minidev.json.annotate.JsonIgnore
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Mottaker
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.Organization
import no.nhn.register.communicationparty.OrganizationPerson
import no.nhn.register.communicationparty.Service
import java.util.*
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart as Avsender
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Virksomhet as FastlegeKontor

abstract class KommunikasjonsPart(val herId: HerId, val navn: String, val aktiv: Boolean = true) {

    enum class Type { Organization, Person, Service }

    init {
        require(aktiv) { "Kommunikasjonspart ${herId.verdi} (${navn} er ikke aktiv" }
    }

    class Virksomhet(herId: HerId, navn: String, aktiv: Boolean = true) :
        KommunikasjonsPart(herId, navn, aktiv) {
        constructor(virksomhet: Organization) :
                this(virksomhet.herId(),
                    virksomhet.name.value,
                    virksomhet.isActive)
    }

    class Fastlege(
        herId: HerId, val name: Navn, val fastlegeKontor: FastlegeKontor, aktiv: Boolean = true,
    ) :
        Tjeneste(herId, name.visningsNavn, fastlegeKontor, aktiv) {
        constructor(person: OrganizationPerson, virksomhet: Organization) :
                this(person.herId(),
                    person.navn(),
                    Virksomhet(virksomhet),
                    person.isActive)
    }

    open class Tjeneste(herId: HerId, navn: String, val virksomhet: Virksomhet, aktiv: Boolean) :
        KommunikasjonsPart(herId, navn, aktiv) {
        constructor(tjeneste: Service, virksomhet: Organization) :
                this(tjeneste.herId(),
                    tjeneste.name.value,
                    Virksomhet(virksomhet),
                    tjeneste.isActive)
    }

    data class Mottaker(val part: KommunikasjonsPart, val navn: Navn)
}

private fun OrganizationPerson.navn() = with(person.value) {
    Navn(firstName.value, middleName.value, lastName.value)
}

data class Innsending(val id: UUID,
                      val parter: Parter,
                      val pasient: Pasient,
                      val vedlegg: ByteArray? = null) {

    @JsonIgnore
    val avsender = parter.avsender.herId

    data class Parter(val avsender: Avsender, val mottaker: Mottaker)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Innsending

        return id == other.id
    }

    override fun hashCode() = id.hashCode()
    override fun toString() =
        javaClass.simpleName + "(id=$id, parter=$parter, pasient=$pasient, vedlegg=${vedlegg?.size})"
}

private fun CommunicationParty.herId() = HerId(herId)

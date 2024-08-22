package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Tjeneste
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.Organization
import no.nhn.register.communicationparty.OrganizationPerson
import no.nhn.register.communicationparty.Service

abstract class KommunikasjonsPart(aktiv: Boolean, val visningsNavn: String?, val herId: HerId, val navn: String) {

    enum class Type { Organization, Person, Service }

    init {
        require(aktiv) { "Kommunikasjonspart ${herId.verdi} (${navn} er ikke aktiv" }
    }

    class Virksomhet(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(virksomhet: Organization) : this(virksomhet.isActive,
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
        constructor(person: OrganizationPerson, virksomhet: Organization) : this(person.isActive,
            person.displayName.value,
            person.herId(),
            person.name.value,
            Virksomhet(virksomhet))
    }

    class Tjeneste(aktiv: Boolean, visningsNavn: String?, herId: HerId, navn: String, val virksomhet: Virksomhet) :
        KommunikasjonsPart(aktiv, visningsNavn, herId, navn) {
        constructor(tjeneste: Service, virksomhet: Organization) :
                this(tjeneste.isActive,
                    tjeneste.displayName.value,
                    tjeneste.herId(),
                    tjeneste.name.value,
                    Virksomhet(virksomhet))
    }
}

data class Bestilling(val tjenester: Tjenester, val pasient: Pasient) {
    data class Tjenester(val fra: Tjeneste, val til: Tjeneste)
}

private fun CommunicationParty.herId() = HerId(herId)
fun Int.herId() = HerId(this)

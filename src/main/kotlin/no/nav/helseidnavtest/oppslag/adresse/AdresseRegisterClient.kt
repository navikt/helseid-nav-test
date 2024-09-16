package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.Innsending.Parter
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Mottaker
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    fun parter(fra: HerId, til: HerId, navn: Navn) =
        Parter(kommunikasjonsPart(fra), Mottaker(kommunikasjonsPart(til), navn))

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi.toInt())

}
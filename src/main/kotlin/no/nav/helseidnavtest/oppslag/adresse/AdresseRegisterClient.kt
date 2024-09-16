package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.other
import no.nav.helseidnavtest.oppslag.adresse.Innsending.Parter
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Tjeneste
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    fun parter(fra: HerId, til: HerId = fra.other(), navn: Navn) =
        Parter(kommunikasjonsPart(fra) as Tjeneste,
            KommunikasjonsPart.Mottaker(kommunikasjonsPart(til), navn))

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi.toInt())

}
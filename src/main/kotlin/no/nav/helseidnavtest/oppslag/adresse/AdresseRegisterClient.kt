package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.other
import no.nav.helseidnavtest.oppslag.adresse.Bestilling.Tjenester
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Tjeneste
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    fun kommunikasjonsParter(fra: HerId, til: HerId = fra.other()) =
        Tjenester(kommunikasjonsPart(fra) as Tjeneste, kommunikasjonsPart(til) as Tjeneste)

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi.toInt())

}
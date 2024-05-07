package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterWSAdapter) {

    fun herIdForHpr(hpr: HprId) = HerId(adapter.herIdForId(hpr.verdi))

    fun herIdForVirksomhet(nummer: Virksomhetsnummer) = HerId(adapter.herIdForId(nummer.verdi))
}
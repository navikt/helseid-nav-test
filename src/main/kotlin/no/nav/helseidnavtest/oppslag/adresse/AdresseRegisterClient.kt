package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Orgnummer
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterWSAdapter) {

    fun herIdForOrgnummer(nummer: Orgnummer) = HerId(adapter.herIdForId(nummer.verdi))
}
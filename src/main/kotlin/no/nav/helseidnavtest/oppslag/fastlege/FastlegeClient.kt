package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
import org.springframework.stereotype.Service

@Service
class FastlegeClient(private val fastlegeAdapter: FastlegeWSAdapter, private val adresseAdapter: AdresseWSAdapter) {
    fun kontor(fnr: Fødselsnummer) = fastlegeAdapter.kontor(fnr).apply {
        herId = orgnummer?.let { adresseAdapter.herId(it) }
    }
}
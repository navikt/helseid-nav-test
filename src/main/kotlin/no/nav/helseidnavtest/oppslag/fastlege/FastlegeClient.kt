package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import org.springframework.stereotype.Service

@Service
class FastlegeClient(private val fastlegeAdapter: FastlegeWSAdapter, private val adresseClient: AdresseRegisterClient) {
    fun kontor(fnr: Fødselsnummer) = fastlegeAdapter.kontor(fnr).apply {
        herId = adresseClient.herIdForVirksomhet(orgnummer)
    }
    fun herId(pasient: Fødselsnummer) = fastlegeAdapter.herId(pasient.verdi)
}
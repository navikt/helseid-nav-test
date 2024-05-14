package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.DialogmeldingClient
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import org.springframework.stereotype.Service

@Service
class FastlegeClient(private val fastlegeAdapter: FastlegeWSAdapter, private val adresseClient: AdresseRegisterClient, private val partnerClient: DialogmeldingClient) {
    fun kontor(pasient: Fødselsnummer) = fastlegeAdapter.kontorViaPasient(pasient.verdi).apply {
        adresseClient.herIdForOrgnummer(orgnummer).let {
            herId = it
            partnerId = partnerClient.partnerId(it, this)
        }
    }
    fun herIdForLegeViaPasient(pasient: Fødselsnummer) = HerId(fastlegeAdapter.herIdForLegeViaPasient(pasient.verdi))
}
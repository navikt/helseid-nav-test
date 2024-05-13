package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.DialogmeldingClient
import no.nav.helseidnavtest.dialogmelding.DialogmeldingRestAdapter
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import org.springframework.stereotype.Service

@Service
class FastlegeClient(private val fastlegeAdapter: FastlegeWSAdapter, private val adresseClient: AdresseRegisterClient, private val partnerClient: DialogmeldingClient) {
    fun kontor(fnr: Fødselsnummer) = fastlegeAdapter.kontor(fnr).apply {
        adresseClient.herIdForVirksomhet(orgnummer).let {
            herId = it
            partnerId = partnerClient.partnerId(it)
        }
    }
    fun herId(pasient: Fødselsnummer) = fastlegeAdapter.herId(pasient.verdi)
}
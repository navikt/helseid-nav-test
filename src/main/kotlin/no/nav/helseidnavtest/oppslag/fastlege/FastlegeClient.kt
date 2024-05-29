package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.DialogmeldingClient
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
@Retryable(include = [RecoverableException::class])
class FastlegeClient(private val fastlegeAdapter: FastlegeWSAdapter, private val adresseClient: AdresseRegisterClient, private val partnerClient: DialogmeldingClient) {
    fun kontorForPasient(pasient: Fødselsnummer) = fastlegeAdapter.kontorViaPasient(pasient.verdi).apply {
        adresseClient.herIdForOrgnummer(orgnummer).let {
            herId = it
            partnerId = partnerClient.partnerId(it, this)
        }
    }

    fun legeFNR(navn: String) = fastlegeAdapter.fastlegeFNR(navn)


    fun lege(pasient: Fødselsnummer) = fastlegeAdapter.lege(pasient.verdi)

    fun herIdForLegeViaPasient(pasient: Fødselsnummer) = HerId(fastlegeAdapter.herIdForLegeViaPasient(pasient.verdi))
}
package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.AvtaleId
import no.nav.helseidnavtest.dialogmelding.DialogmeldingClient
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
@Retryable(include = [RecoverableException::class])
class FastlegeClient(private val fastlegeAdapter: FastlegeCXFAdapter, private val adresseClient: AdresseRegisterClient, private val partnerClient: DialogmeldingClient) {
    fun kontorForPasient(pasient: Fødselsnummer) = fastlegeAdapter.kontorViaPasient(pasient.verdi).apply {
        adresseClient.herIdForOrgnummer(orgnummer).let {
            herId = it
            partnerId = partnerClient.partnerId(it, this)
        }
    }

    fun pasienterForAvtale(id: AvtaleId) = fastlegeAdapter.pasienterForAvtale(id)

    fun legeFNR(navn: String) = fastlegeAdapter.pasienterForFastlege(navn)

    fun lege(pasient: Fødselsnummer) = fastlegeAdapter.lege(pasient.verdi)

    fun herIdForLegeViaPasient(pasient: Fødselsnummer) = HerId(fastlegeAdapter.herIdForLegeViaPasient(pasient.verdi))
}
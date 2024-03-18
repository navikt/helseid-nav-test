package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.stereotype.Component

@Component
class ArbeidClient(private val arbeid : ArbeidRestClientAdapter,
                   private val org : OrganisasjonRestClientAdapter) {

    fun arbeidInfo(fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr).map {
        it.tilArbeidInfo(org.orgNavn(it.arbeidsgiver.organisasjonsnummer))
    }
}
package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid

import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon.OrganisasjonRestClientAdapter
import org.springframework.stereotype.Component

@Component
class ArbeidClient(private val arbeid : ArbeidRestClientAdapter,
                   private val org : OrganisasjonRestClientAdapter
) {

    fun arbeidInfo(fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr).map {
        it.tilArbeidInfo(org.orgNavn(it.arbeidsgiver.organisasjonsnummer))
    }
}
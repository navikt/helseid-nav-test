package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.helse.helseidnavtest.helseopplysninger.error.handleErrors
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.AbstractRestClientAdapter
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
@Component
class ArbeidRestClientAdapter(@Qualifier(ARBEID) restClient : RestClient, private val cf : ArbeidConfig) : AbstractRestClientAdapter(restClient, cf) {

    fun arbeidInfo(fnr: Fødselsnummer) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri(cf::arbeidsforholdURI)
                .header(NAV_PERSONIDENT_HEADER, fnr.fnr)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res ->
                   handleErrors(req, res, fnr.fnr)
                }
                .body<List<ArbeidsforholdDTO>>().also {
                    log.trace("Arbeidsforhold response {}", it)
                } ?: listOf()
        }
        else {
            listOf()
        }
    override fun toString() = "${javaClass.simpleName} [webClient=$restClient, cfg=$cf]"

    companion object {
        private const val NAV_PERSONIDENT_HEADER = "Nav-Personident"
    }

}
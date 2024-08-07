package no.nav.helseidnavtest.oppslag.arbeid

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler
import org.springframework.web.client.body

@Component
class ArbeidRestClientAdapter(@Qualifier(ARBEID) restClient: RestClient,
                              private val handler: ErrorHandler,
                              private val cf: ArbeidConfig) :
    AbstractRestClientAdapter(restClient, cf) {

    fun arbeidInfo(fnr: Fødselsnummer) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri(cf::arbeidsforholdURI)
                .header(NAV_PERSONIDENT_HEADER, fnr.verdi)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
                .body<List<ArbeidsforholdDTO>>().also {
                    log.trace("Arbeidsforhold response {}", it)
                } ?: listOf()
        } else {
            listOf()
        }

    override fun toString() = "${javaClass.simpleName} [webClient=$restClient, cfg=$cf]"

    companion object {
        private const val NAV_PERSONIDENT_HEADER = "Nav-Personident"
    }
}
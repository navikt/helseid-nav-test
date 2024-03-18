package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.helse.helseidnavtest.helseopplysninger.error.IntegrationException
import no.nav.helse.helseidnavtest.helseopplysninger.error.OppslagNotFoundException
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
@Component
class ArbeidRestClientAdapter(@Qualifier(ARBEID) restClient : RestClient, private val cf : ArbeidConfig) : AbstractRestClientAdapter(restClient, cf) {

    fun arbeidInfo(fnr: Fødselsnummer) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri(cf::arbeidsforholdURI)
                .header("Nav-Personident", fnr.fnr)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res ->
                   handleErrors(req, res, fnr)
                }
                .onStatus({ it.is2xxSuccessful }) { req, res ->
                    log.trace("Received {} from {}", res.statusCode, req.uri)
                }
                .body<List<ArbeidsforholdDTO>>().also {
                    log.trace("Arbeidsforhold response {}", it)
                } ?: listOf()
        }
        else {
            listOf()
        }

    private fun handleErrors(req: HttpRequest, res: ClientHttpResponse, fnr: Fødselsnummer) {
        log.warn("Received ${res.statusCode} from ${req.uri}" )
        throw when (res.statusCode) {
            HttpStatus.NOT_FOUND -> OppslagNotFoundException("Fant ikke arbeidsforhodl for ${fnr.fnr}")
            else -> IntegrationException("Fikk response ${res.statusCode} fra ${req.uri}")
        }
    }

    override fun toString() = "${javaClass.simpleName} [webClient=$restClient, cfg=$cf]"
}
package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
@Component
class ArbeidRestClientAdapter(@Qualifier(ARBEID) restClient : RestClient, private val cf : ArbeidConfig) : AbstractRestClientAdapter(restClient, cf) {

    fun arbeidInfo() =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri(cf::arbeidsforholdURI)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res ->
                    log.warn("Received ${res.statusCode} from ${req.uri}" )
                }
                .onStatus({ it.is2xxSuccessful }) { req, res ->
                    log.trace("Received {} from {}", res.statusCode, req.uri)
                }
                .body<List<ArbeidsforholdDTO>>() ?: listOf()
        }
        else {
            listOf()
        }

    override fun toString() = "${javaClass.simpleName} [webClient=$restClient, cfg=$cf]"
}
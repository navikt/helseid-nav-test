package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBACTION
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBROLE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBSERVICE
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.error.handleErrors
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class DialogmeldingRestAdapter(private val cf: DialogmeldingConfig, @Qualifier(DIALOGMELDING) client : RestClient) : AbstractRestClientAdapter(client, cf) {

    fun partnerId(herId: String) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri {
                    it.path(cf.path)
                        .queryParam("service", EBSERVICE)
                        .queryParam("role", EBROLE)
                        .queryParam("action", EBACTION)
                        .build(herId)
                }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res -> handleErrors(req, res, herId) }
                .body<String>().also {
                    log.trace("Dialogmelding partner response {}", it)
                } ?: throw NotFoundException("Fant ikke partnerId for herId $herId","",cf.baseUri)
        }
        else {
            throw NotImplementedError("Dialogmelding er ikke aktivert")
        }
}

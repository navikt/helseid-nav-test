package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBACTION
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBROLE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBSERVICE
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler
import org.springframework.web.client.body

@Component
class DialogmeldingRestAdapter(private val cf: DialogmeldingConfig,
                               @Qualifier(DIALOGMELDING) client: RestClient,
                               private val handler: ErrorHandler) :
    AbstractRestClientAdapter(client, cf) {

    fun partnerId(herId: String, behandlerKontor: BehandlerKontor) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri { b ->
                    b.path(cf.path)
                        .queryParam("service", EBSERVICE)
                        .queryParam("role", EBROLE)
                        .queryParam("action", EBACTION)
                        .build(herId).also {
                            log.trace("Dialogmelding partner request URL {}", it)
                        }
                }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
                .body<String>().also {
                    log.trace("Dialogmelding partner response {}", it)
                }
                ?: throw NotFoundException(baseUri, "Fant ikke partnerId for herId $herId for ${behandlerKontor.navn}")
        } else {
            throw NotImplementedError("Dialogmelding oppslag er ikke aktivert")
        }
}

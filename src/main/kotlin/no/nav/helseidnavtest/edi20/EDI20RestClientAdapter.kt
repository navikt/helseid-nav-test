package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.Apprec.Companion.OK
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.error.BodyConsumingErrorHandler
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.*
import java.util.Base64.getEncoder
import kotlin.text.Charsets.UTF_8

@Component
class EDI20RestClientAdapter(
    @Qualifier(EDI20) restClient: RestClient,
    private val cf: EDI20Config,
    private val generator: EDI20DialogmeldingGenerator,
    @Qualifier(EDI20) private val handler: BodyConsumingErrorHandler
) : AbstractRestClientAdapter(restClient, cf) {

    fun apprec(herId: HerId, id: UUID) =
        restClient
            .post()
            .uri { cf.apprecURI(it, id, herId) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .body(OK)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<String>()

    fun status(herId: HerId, id: UUID) =
        restClient
            .get()
            .uri { cf.statusURI(it, id) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<List<Status>>()

    fun les(herId: HerId, id: UUID) =
        restClient
            .get()
            .uri { cf.lesURI(it, id) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_XML)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<String>()?.let(generator::fraApprec)

    fun poll(herId: HerId, appRec: Boolean) =
        restClient
            .get()
            .uri { cf.pollURI(it, herId, appRec) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<List<Meldinger>>()

    fun send(herId: HerId, hodemelding: String) =
        restClient
            .post()
            .uri(cf::sendURI)
            .headers { it.herId(herId.verdi) }
            .accept(APPLICATION_JSON)
            .body(BusinessDocument(hodemelding.encode()))
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()
            .headers.location?.key() ?: throw IllegalStateException("No location header")

    fun konsumert(herId: HerId, id: UUID) =
        restClient
            .put()
            .uri { cf.lestURI(it, id, herId) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()

    private fun String.encode() = getEncoder().encodeToString(toByteArray(UTF_8))

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"
}
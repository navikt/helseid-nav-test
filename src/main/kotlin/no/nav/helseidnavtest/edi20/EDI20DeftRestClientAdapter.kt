package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler
import org.springframework.web.client.body
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Component
class EDI20DeftRestClientAdapter(@Qualifier(EDI20DEFT) restClient: RestClient,
                                 private val cf: EDI20DeftConfig,
                                 @Qualifier(EDI20) private val handler: ErrorHandler) :
    AbstractRestClientAdapter(restClient, cf) {

    fun les(herid: HerId, uri: URI) =
        restClient
            .get()
            .uri { uri }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<String>()

    fun kvitter(herid: HerId, key: String) =
        restClient
            .put()
            .uri { cf.kvitteringURI(it, key, herid) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()

    fun status(herid: HerId, key: String) =
        restClient
            .get()
            .uri { cf.statusURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<DeftStatus>()

    fun slett(herid: HerId, key: String) =
        restClient
            .delete()
            .uri { cf.deleteURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()

    fun upload(herid: HerId, file: MultipartFile) =
        restClient
            .post()
            .uri { cf.uploadURI(it, herid) }
            .headers {
                it.contentDisposition = contentDisposition(file.originalFilename)
                it.herId(herid)
            }
            .body(LinkedMultiValueMap<String, Any>().apply {
                add("file", file.resource)
            })
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()
            .headers.location ?: throw IllegalStateException("No location header")

    private fun contentDisposition(originalFilename: String?) =
        ContentDisposition.inline()
            .filename(originalFilename)
            .build()

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
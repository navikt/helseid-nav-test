package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.error.ErrorHandler
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Component
class EDI20DeftRestClientAdapter(@Qualifier(EDI20DEFT) restClient: RestClient,
                                 private val cf: EDI20DeftConfig,
                                 private val handler: ErrorHandler) :
    AbstractRestClientAdapter(restClient, cf) {

    fun les(uri: URI, herid: String) =
        restClient
            .get()
            .uri { uri }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res ->
                handler.handle(req, res)
            }
            .body<String>()

    fun kvitter(key: String, herid: String) =
        restClient
            .put()
            .uri { cf.kvitteringURI(it, key, herid) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res ->
                handler.handle(req, res)
            }
            .toBodilessEntity()

    fun status(key: String, herid: String) =
        restClient
            .get()
            .uri { cf.statusURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res ->
                handler.handle(req, res)
            }
            .body<DeftStatus>()

    fun slett(key: String, herid: String) =
        restClient
            .delete()
            .uri { cf.deleteURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .onStatus({ it.isError }) { req, res ->
                handler.handle(req, res)
            }
            .toBodilessEntity()

    fun upload(file: MultipartFile, herid: String) =
        restClient
            .post()
            .uri { cf.uploadURI(it, herid) }
            .headers {
                it.contentDisposition = ContentDisposition
                    .inline()
                    .filename(file.originalFilename)
                    .build()
                it.herId(herid)
            }
            .body(LinkedMultiValueMap<String, Any>().apply {
                add("file", file.resource)
            })
            .retrieve()
            .onStatus({ it.isError }) { req, res ->
                handler.handle(req, res)
            }
            .toBodilessEntity()
            .headers.location ?: throw IllegalStateException("No location header")

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
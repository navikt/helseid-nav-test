package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
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
class EDI20DeftRestClientAdapter(@Qualifier(EDI20DEFT) restClient: RestClient, private val cf: EDI20DeftConfig) :
    AbstractRestClientAdapter(restClient, cf) {

    fun les(uri: URI, herid: String) =
        restClient
            .get()
            .uri { uri }
            .headers { it.herId(herid) }
            .retrieve()
            .body<String>()

    fun status(key: String, herid: String) =
        restClient
            .get()
            .uri { cf.statusURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .body<DeftStatus>()

    fun slett(key: String, herid: String) =
        restClient
            .delete()
            .uri { cf.deleteURI(it, key) }
            .headers { it.herId(herid) }
            .retrieve()
            .toBodilessEntity()

    fun upload(file: MultipartFile, herId: String) =
        restClient
            .post()
            .uri { cf.uploadURI(it, herId) }
            .headers {
                it.contentDisposition = ContentDisposition
                    .inline()
                    .filename(file.originalFilename)
                    .build()
                it.herId(herId)
            }
            .body(LinkedMultiValueMap<String, Any>().apply {
                add("file", file.resource)
            })
            .retrieve()
            .toBodilessEntity()
            .headers.location ?: throw IllegalStateException("No location header")

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
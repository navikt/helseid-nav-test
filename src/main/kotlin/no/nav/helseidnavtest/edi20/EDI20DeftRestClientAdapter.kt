package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

@Component
class EDI20DeftRestClientAdapter(@Qualifier(EDI20DEFT) restClient: RestClient, private val cf: EDI20DeftConfig) : AbstractRestClientAdapter(restClient,cf) {

    fun upload(file: MultipartFile, herId: String) =
        restClient
            .post()
            .uri { cf.uploadURI(it,herId) }
            .headers {
                it.contentDisposition = ContentDisposition
                    .inline()
                    .filename(file.originalFilename)
                    .build()
                it.herIdHeader(herId)
            }
            .body(LinkedMultiValueMap<String, Any>().apply {
                add("file", file.resource)
            })
            .retrieve()
            .toBodilessEntity()
            .headers.location

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
package no.nav.helseidnavtest.edi20

import com.ibm.disthub2.impl.formats.OldEnvelop.payload.normal.body
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.net.URI


@Component
class EDI20DeftRestClientAdapter(@Qualifier(EDI20DEFT) restClient: RestClient, private val cf: EDI20DeftConfig) : AbstractRestClientAdapter(restClient,cf) {

    fun upload(stream: InputStream, herId: String): URI? {

        val headers = HttpHeaders().apply {
            contentType = MULTIPART_FORM_DATA
        }
         val body = LinkedMultiValueMap<String, Any>().apply {
            add("file",  ByteArrayResource(stream.readAllBytes()))
        }

        val requestEntity = HttpEntity<MultiValueMap<String, Any>>(body, headers)

        return restClient
            .post()
            .uri { cf.uploadURI(it,herId) }
            .headers {
                it.contentDisposition = ContentDisposition
                    .inline()
                    .filename("Filename.xml")
                    .build()
                it.herIdHeader(herId)
            }
            .body(requestEntity)
            .retrieve()
            .toBodilessEntity()
            .headers.location
    }



    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
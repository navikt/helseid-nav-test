package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.net.URI


@Component
class EDI20DeftRestClientAdapter(@Qualifier(EDI20) restClient: RestClient, private val cf: EDI20DeftConfig) : AbstractRestClientAdapter(restClient,cf) {

    fun upload(bytes: ByteArray, herId: String): URI? {

        val parts = LinkedMultiValueMap<String, Any>().apply {
            add("file", bytes)
        }
        return restClient
            .post()
            .uri { cf.uploadURI(it,herId) }
            .headers {
                it.contentDisposition = ContentDisposition
                    .attachment()
                    .filename("Filename")
                    .build()
                it.herIdHeader(herId)
            }
            .body(parts)
            .retrieve()
            .toBodilessEntity()
            .headers.location
    }



    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

}
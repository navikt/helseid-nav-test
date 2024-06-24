package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class EDI20RestClientAdapter(@Qualifier(EDI20) restClient: RestClient, private val cf: EDI20Config) : AbstractRestClientAdapter(restClient,cf) {



    @Retryable(include = [RecoverableException::class])
    fun messages() =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri(cf::messagesURI)
                .accept(APPLICATION_JSON)
                .retrieve()
                //.onStatus({ !it.is2xxSuccessful }) { _, res ->
                //    log.error("Error in messages request ${res.statusCode} ")
                //}
                .body<String>().also { log.trace("Messages response {}", it) }
                .also { log.trace("Response {}", it) }
        }
    else  throw NotImplementedError("Messages not available")


    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

    companion object {

        private val MESSAGES = "messages"
    }
}
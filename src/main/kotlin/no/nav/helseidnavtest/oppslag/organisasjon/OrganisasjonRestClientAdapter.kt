package no.nav.helseidnavtest.oppslag.organisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import no.nav.helseidnavtest.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler
import org.springframework.web.client.body

@Component
class OrganisasjonRestClientAdapter(@Qualifier(ORGANISASJON) val client: RestClient,
                                    private val cf: OrganisasjonConfig, private val handler: ErrorHandler) :
    AbstractRestClientAdapter(client, cf) {

    @Retryable(include = [RecoverableException::class])
    fun orgNavn(orgnr: OrgNummer) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri { b -> cf.organisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
                .body<OrganisasjonDTO>()?.fulltNavn ?: orgnr.verdi
                .also { log.trace("Organisasjon ${orgnr.verdi} response {}", it) }
        } else {
            orgnr.verdi
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OrganisasjonDTO(val navn: OrganisasjonNavnDTO) {
    val fulltNavn = with(navn) {
        listOfNotNull(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5).joinToString(" ")
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class OrganisasjonNavnDTO(val navnelinje1: String?,
                                            val navnelinje2: String?,
                                            val navnelinje3: String?,
                                            val navnelinje4: String?,
                                            val navnelinje5: String?)
}
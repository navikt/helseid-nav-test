package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.helseidnavtest.helseopplysninger.error.RecoverableException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.helse.helseidnavtest.helseopplysninger.error.handleErrors
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.AbstractRestClientAdapter
import org.springframework.retry.annotation.Retryable
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class OrganisasjonRestClientAdapter(@Qualifier(ORGANISASJON) val client: RestClient,
                                    private val cf: OrganisasjonConfig
) : AbstractRestClientAdapter(client, cf) {


    @Retryable(include = [RecoverableException::class])
        fun orgNavn(orgnr: OrgNummer) =
        if (cf.isEnabled) {
            restClient
                .get()
                .uri { b -> cf.organisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ !it.is2xxSuccessful }) { req, res ->
                    handleErrors(req, res, orgnr.orgnr)
                }
                .body<OrganisasjonDTO>()?.fulltNavn ?: orgnr.orgnr
                .also { log.trace("Organisasjon oppslag response {}", it) }
        }
        else {
            orgnr.orgnr
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
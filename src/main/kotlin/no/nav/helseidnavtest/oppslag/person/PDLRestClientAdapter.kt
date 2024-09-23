package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import no.nav.helseidnavtest.oppslag.person.PDLMapper.pdlPersonTilPerson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler

@Component
class PDLRestClientAdapter(@Qualifier(PDL) private val graphQlClient: GraphQlClient,
                           @Qualifier(PDL) restClient: RestClient,
                           private val handler: ErrorHandler,
                           cfg: PDLConfig) : AbstractGraphQLAdapter(restClient, cfg) {

    override fun ping(): Map<String, String> {
        restClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()
        return emptyMap()
    }

    fun person(fnr: Fødselsnummer) =
        with(fnr) {
            query<PDLPersonResponse>(graphQlClient, PERSON_QUERY, mapOf(IDENT to verdi))?.active?.let {
                pdlPersonTilPerson(it, this)
            } ?: throw NotFoundException(baseUri, "Fant ikke $fnr")
        }

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient,graphQlClient=$graphQlClient, cfg=$cfg]"

    companion object {
        private val IDENT = "ident"
        private val PERSON_QUERY = "query-person" to "hentPerson"
    }
}
package no.nav.helseidnavtest.oppslag.person

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.*
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import no.nav.helseidnavtest.oppslag.person.PDLMapper.pdlPersonTilPerson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.GraphQlClient
import org.springframework.web.reactive.function.client.WebClient

@Component
class PDLWebClientAdapter(private val graphQlClient : GraphQlClient, @Qualifier(PDL) webClient: WebClient, cfg : PDLConfig, errorHandler: GraphQLErrorHandler) : AbstractGraphQLAdapter(webClient,cfg, errorHandler) {

    override fun ping() : Map<String, String> {
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .contextCapture()
            .block()
        return emptyMap()
    }


    fun person(fnr: Fødselsnummer) =
        with(fnr) {
            query<PDLPersonResponse>(graphQlClient, PERSON_QUERY, mapOf(IDENT to this.fnr))?.active?.let {
                pdlPersonTilPerson(it, this)
            } ?: throw NotFoundGraphQLException("Fant ikke $fnr",cfg.baseUri)
        }

    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,graphQlClient=$graphQlClient, cfg=$cfg]"

    companion object {

        private val IDENT = "ident"
        private  val PERSON_QUERY = "query-person" to "hentPerson"
    }
}
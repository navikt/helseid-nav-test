package no.nav.helseidnavtest.oppslag.person

import org.springframework.graphql.client.GraphQlClient
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.*
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import no.nav.helseidnavtest.oppslag.person.PDLMapper.pdlSøkerTilSøker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.WebClient

@Component
class PDLWebClientAdapter(@Qualifier(PDL) private val graphQlClient : HttpGraphQlClient, @Qualifier(PDL) webClient: WebClient, cfg : PDLConfig, errorHandler: GraphQLErrorHandler) : AbstractGraphQLAdapter(webClient,cfg, errorHandler) {

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


    fun søker(fnr: Fødselsnummer) =
        with(fnr) {
            query<PDLWrappedSøker>(graphQlClient, PERSON_QUERY, mapOf(IDENT to fnr.fnr))?.active?.let {
                pdlSøkerTilSøker(it, this)
            } ?: throw NotFoundGraphQLException("Fant ikke søker for $fnr")
        }

    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,graphQlClient=graphQlClient, cfg=$cfg]"

    companion object {

        private val IDENT = "ident"
        private  val PERSON_QUERY = Pair("query-person","hentPerson")
    }
}
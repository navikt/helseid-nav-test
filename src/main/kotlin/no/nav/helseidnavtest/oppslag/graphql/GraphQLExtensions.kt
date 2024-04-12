package no.nav.helseidnavtest.oppslag.graphql

import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.*
import no.nav.helseidnavtest.error.RecoverableGraphQLException.*
import org.slf4j.LoggerFactory.*
import org.springframework.graphql.ResponseError
import org.springframework.graphql.client.FieldAccessException

object GraphQLExtensions {

    private const val Unauthorized = "unauthorized"
    private const val Unauthenticated = "unauthenticated"
    private const val BadRequest = "bad_request"
    private const val NotFound = "not_found"
    private val log = getLogger(javaClass)
    fun FieldAccessException.oversett() = response.errors.oversett(message)
    private fun List<ResponseError>.oversett(message: String?) = oversett(firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil")
        .also { e ->
            log.warn("GraphQL oppslag returnerte $size feil. $this, oversatte feilkode $message til ${e.javaClass.simpleName}", this)
        }

    private fun oversett(kode : String?, msg : String) =
        when (kode) {
            Unauthorized -> UnauthorizedGraphQLException(msg)
            Unauthenticated -> UnauthenticatedGraphQLException(msg)
            BadRequest -> BadGraphQLException(msg)
            NotFound -> NotFoundGraphQLException(msg)
            else -> UnhandledGraphQLException(msg)
        }
}
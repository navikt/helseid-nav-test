package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.error.IrrecoverableGraphQLException
import no.nav.helseidnavtest.error.RecoverableGraphQLException
import org.slf4j.LoggerFactory
import org.springframework.graphql.ResponseError
import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus.*

object GraphQLExtensions {

    private const val Unauthorized = "unauthorized"
    private const val Unauthenticated = "unauthenticated"
    private const val BadRequest = "bad_request"
    private const val NotFound = "not_found"
    private val log = LoggerFactory.getLogger(javaClass)
    fun FieldAccessException.oversett() = response.errors.oversett(message)
    private fun List<ResponseError>.oversett(message: String?) = oversett(firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil")
        .also { e ->
            log.warn("GraphQL oppslag returnerte $size feil. $this, oversatte feilkode til ${e.javaClass.simpleName}", this)
        }

    private fun oversett(kode : String?, msg : String) =
        when (kode) {
            Unauthorized -> IrrecoverableGraphQLException.UnauthorizedGraphQLException(UNAUTHORIZED, msg)
            Unauthenticated -> IrrecoverableGraphQLException.UnauthenticatedGraphQLException(FORBIDDEN, msg)
            BadRequest -> IrrecoverableGraphQLException.BadGraphQLException(BAD_REQUEST, msg)
            NotFound -> IrrecoverableGraphQLException.NotFoundGraphQLException(NOT_FOUND, msg)
            else -> RecoverableGraphQLException.UnhandledGraphQLException(INTERNAL_SERVER_ERROR, msg)
        }

}
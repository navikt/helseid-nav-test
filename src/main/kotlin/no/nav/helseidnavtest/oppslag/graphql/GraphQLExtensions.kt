package no.nav.helseidnavtest.oppslag.graphql

import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.*
import no.nav.helseidnavtest.error.RecoverableGraphQLException.*
import org.slf4j.LoggerFactory.*
import org.springframework.graphql.ResponseError
import org.springframework.graphql.client.FieldAccessException
import java.net.URI

object GraphQLExtensions {

    private const val Unauthorized = "unauthorized"
    private const val Unauthenticated = "unauthenticated"
    private const val BadRequest = "bad_request"
    private const val NotFound = "not_found"
    private val log = getLogger(javaClass)
    fun FieldAccessException.oversett(uri: URI) = response.errors.oversett(message,uri)
    private fun List<ResponseError>.oversett(message: String?, uri: URI) = oversett(firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil", uri)
        .also { e ->
            log.warn("GraphQL oppslag returnerte $size feil, oversatte $message til ${e.javaClass.simpleName}", this)
        }

    private fun oversett(kode : String?, msg : String, uri: URI) =
        when (kode) {
            Unauthorized -> UnauthorizedGraphQLException(msg, uri)
            Unauthenticated -> UnauthenticatedGraphQLException(msg,uri)
            BadRequest -> BadGraphQLException(msg,uri)
            NotFound -> NotFoundGraphQLException(msg,uri)
            else -> UnhandledGraphQLException(msg,uri)
        }
}
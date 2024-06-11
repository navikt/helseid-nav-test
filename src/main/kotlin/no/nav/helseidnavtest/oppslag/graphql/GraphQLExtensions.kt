package no.nav.helseidnavtest.oppslag.graphql

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.NotFoundException
import org.slf4j.LoggerFactory.*
import org.springframework.graphql.ResponseError
import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus.*
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
            Unauthorized -> IrrecoverableException(UNAUTHORIZED, "Uautorisert", msg, uri)
            Unauthenticated -> IrrecoverableException(FORBIDDEN, "Uautentisert", msg, uri)
            BadRequest -> IrrecoverableException(BAD_REQUEST, "Bad request", msg, uri)
            NotFound -> NotFoundException(detail = msg, uri = uri)
            else -> IrrecoverableException(INTERNAL_SERVER_ERROR, "Ukjent feil", msg, uri)
        }
}
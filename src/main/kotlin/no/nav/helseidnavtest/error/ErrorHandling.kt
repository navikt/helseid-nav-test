package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.client.ClientHttpResponse
import java.net.URI

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String) {
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException("Fant ikke noe for $detail")
        else -> RecoverableException("Fikk response ${res.statusCode} fra ${req.uri}")
    }
}
abstract class IntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : RuntimeException(msg, cause)

open class RecoverableIntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : IntegrationException(msg, uri, cause)

open class IrrecoverableIntegrationException(msg : String?, uri : URI? = null, cause : Throwable? = null) : IntegrationException(msg, uri, cause)

abstract class IrrecoverableGraphQLException(val status : HttpStatus, msg : String?, cause : Throwable? = null) : IrrecoverableIntegrationException("$msg (${status.value()})", null, cause) {

    class NotFoundGraphQLException(msg : String?) : IrrecoverableGraphQLException(NOT_FOUND, msg)
    class BadGraphQLException(msg : String?, cause : Throwable? = null) : IrrecoverableGraphQLException(BAD_REQUEST, msg,cause)
    class UnauthenticatedGraphQLException(msg : String?) : IrrecoverableGraphQLException(FORBIDDEN, msg)
    class UnauthorizedGraphQLException(msg : String) : IrrecoverableGraphQLException(UNAUTHORIZED, msg)
}

abstract class RecoverableGraphQLException(status : HttpStatus, msg : String?, cause : Throwable?) : RecoverableIntegrationException("${status.value()}-$msg", cause = cause) {

    class UnhandledGraphQLException(msg : String, cause : Throwable? = null) : RecoverableGraphQLException(INTERNAL_SERVER_ERROR, msg, cause)
}

class NotFoundException(msg: String?, cause: Throwable? = null) : IrrecoverableException(msg, cause)
open class IrrecoverableException(msg: String?, cause: Throwable? = null) : IntegrationException(msg, cause = cause)
class RecoverableException(msg: String?, cause: Throwable? = null) : IntegrationException(msg, cause = cause)
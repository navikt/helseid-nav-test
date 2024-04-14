package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.client.ClientHttpResponse
import java.net.URI

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String) {
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException("Fant ikke noe for $detail", req.uri)
        else -> RecoverableException("Fikk response ${res.statusCode}", req.uri)
    }
}



abstract class IrrecoverableGraphQLException(val status : HttpStatus, msg : String?, uri: URI, cause: Throwable? = null) : IrrecoverableException("$msg (${status.value()})", uri, cause) {

    class NotFoundGraphQLException(msg : String?, uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(NOT_FOUND, msg,uri,cause)
    class BadGraphQLException(msg : String?,  uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(BAD_REQUEST, msg,uri,cause)
    class UnauthenticatedGraphQLException(msg : String?, uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(FORBIDDEN, msg,uri,cause)
    class UnauthorizedGraphQLException(msg : String, uri: URI,cause: Throwable? =null) : IrrecoverableGraphQLException(UNAUTHORIZED, msg,uri,cause)
}

abstract class RecoverableGraphQLException(msg : String?, uri: URI,cause: Throwable? = null) : RecoverableException(msg,uri,cause) {

    class UnhandledGraphQLException(msg : String, uri: URI,cause: Throwable? = null) : RecoverableGraphQLException(msg, uri,cause)
}

class NotFoundException(msg: String?, uri: URI, cause: Throwable? = null) : IrrecoverableException(msg, uri, cause)
open class IrrecoverableException(msg: String?, uri: URI? = null, cause: Throwable? = null) :RuntimeException("$uri $msg", cause)
open class RecoverableException(msg: String?, uri: URI? = null,cause: Throwable? = null) : RuntimeException("$uri $msg", cause)
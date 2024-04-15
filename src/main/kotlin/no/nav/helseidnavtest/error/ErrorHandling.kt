package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.ErrorResponseException
import java.net.URI

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String) {
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException("Fant ikke noe for $detail", req.uri)
        else -> RecoverableException("Fikk response ${res.statusCode}", req.uri,BAD_REQUEST)
    }
}

abstract class IrrecoverableGraphQLException(val status : HttpStatus, msg : String?, uri: URI, cause: Throwable? = null) : IrrecoverableException(msg, uri,status, cause) {

    class NotFoundGraphQLException(msg : String?, uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(NOT_FOUND, msg,uri,cause)
    class BadGraphQLException(msg : String?,  uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(BAD_REQUEST, msg,uri,cause)
    class UnauthenticatedGraphQLException(msg : String?, uri: URI,cause: Throwable? = null) : IrrecoverableGraphQLException(FORBIDDEN, msg,uri,cause)
    class UnauthorizedGraphQLException(msg : String, uri: URI,cause: Throwable? =null) : IrrecoverableGraphQLException(UNAUTHORIZED, msg,uri,cause)
}

abstract class RecoverableGraphQLException(msg : String?, uri: URI,status : HttpStatus,cause: Throwable? = null) : RecoverableException(msg,uri,status,cause) {
    class UnhandledGraphQLException(msg : String, uri: URI,cause: Throwable? = null) : RecoverableGraphQLException(msg, uri,BAD_REQUEST,cause)
}

class NotFoundException(msg: String?, uri: URI, cause: Throwable? = null) : IrrecoverableException(msg, uri,NOT_FOUND, cause)
open class IrrecoverableException(msg: String?, uri: URI? = null, status : HttpStatus,cause: Throwable? = null) : ErrorResponseException(status, cause)
open class RecoverableException(msg: String?, uri: URI? = null,status : HttpStatus,cause: Throwable? = null) : ErrorResponseException(status,cause)
package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ProblemDetail.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.ErrorResponseException
import java.net.URI

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String): Nothing =
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException(detail = detail, uri = req.uri)
        else -> RecoverableException(BAD_REQUEST, "Fikk response ${res.statusCode}", req.uri)
    }

class NotFoundException(title: String? = "Ikke funnet", detail: String, uri: URI, cause: Throwable? = null) : IrrecoverableException(NOT_FOUND, title, detail, uri, cause)
open class IrrecoverableException(status: HttpStatus, title: String? = null, detail: String, uri: URI? = null, cause: Throwable? = null) :
   ErrorResponseException(status, forStatusAndDetail(status,detail).apply { this.title=title
        properties = mapOf("uri" to "$uri") },  cause)
open class RecoverableException(status: HttpStatus, detail: String, uri: URI? = null, cause: Throwable? = null):ErrorResponseException(status, forStatusAndDetail(status,detail).apply { title=detail
    properties = mapOf("uri" to "$uri") }, cause)
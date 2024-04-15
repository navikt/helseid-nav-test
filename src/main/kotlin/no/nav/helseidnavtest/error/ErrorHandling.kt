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
        NOT_FOUND -> NotFoundException("Fant ikke noe for $detail", req.uri)
        else -> RecoverableException("Fikk response ${res.statusCode}", req.uri,BAD_REQUEST)
    }

class NotFoundException(msg: String, uri: URI, cause: Throwable? = null) : IrrecoverableException(msg, uri,NOT_FOUND, cause)
open class IrrecoverableException(msg: String, uri: URI? = null, status : HttpStatus,cause: Throwable? = null) :
    ErrorResponseException(status, forStatusAndDetail(status,msg).apply { title=msg
        properties = mapOf("uri" to "$uri") }, cause)
open class RecoverableException(msg: String, uri: URI? = null,status : HttpStatus,cause: Throwable? = null):ErrorResponseException(status, forStatusAndDetail(status,msg).apply { title=msg
    properties = mapOf("uri" to "$uri") }, cause)
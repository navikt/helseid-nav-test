package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail.forStatusAndDetail
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.ErrorResponseException
import java.net.URI

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String): Nothing =
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException(detail = res.body.readAllBytes().decodeToString(), uri = req.uri)
        else -> RecoverableException(res.statusCode as HttpStatus, "Fikk response ${res.statusCode}", req.uri)
    }

class NotFoundException(title: String = "Ikke funnet", detail: String? = null, uri: URI, cause: Throwable? = null) :
    IrrecoverableException(NOT_FOUND, title, detail, uri, cause)

open class IrrecoverableException(status: HttpStatus,
                                  title: String,
                                  detail: String? = null,
                                  uri: URI? = null,
                                  cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, title, detail, uri), cause)

open class RecoverableException(status: HttpStatus, detail: String, uri: URI? = null, cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, "title", detail, uri), cause)

private fun problemDetail(status: HttpStatus, title: String?, detail: String? = "", uri: URI?) =
    forStatusAndDetail(status, detail).apply {
        this.title = title
        properties = mapOf("uri" to "$uri")
    }
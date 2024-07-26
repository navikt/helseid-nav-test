package no.nav.helseidnavtest.error

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail.forStatusAndDetail
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.ErrorResponseException
import org.springframework.web.client.RestClient
import java.net.URI

@Component
class ErrorHandler(private val mapper: ObjectMapper) : RestClient.ResponseSpec.ErrorHandler {
    override fun handle(req: HttpRequest, res: ClientHttpResponse) {
        val respons = mapper.readValue<ErrorResponse>(res.body)
        throw when (res.statusCode) {
            NOT_FOUND -> NotFoundException(detail = respons.error,
                stackTrace = respons.stackTrace,
                uri = req.uri)

            else -> RecoverableException(res.statusCode as HttpStatus, "Fikk response ${res.statusCode}", req.uri)
        }
    }
}

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String): Nothing =
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException(detail = detail, uri = req.uri)
        else -> RecoverableException(res.statusCode as HttpStatus, "Fikk response ${res.statusCode}", req.uri)
    }

class NotFoundException(title: String = "Ikke funnet",
                        detail: String? = null,
                        uri: URI,
                        stackTrace: String? = null,
                        cause: Throwable? = null) :
    IrrecoverableException(NOT_FOUND, title, detail, uri, stackTrace, cause)

open class IrrecoverableException(status: HttpStatus,
                                  title: String,
                                  detail: String? = null,
                                  uri: URI? = null,
                                  stackTrace: String? = null,
                                  cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, title, detail, uri, stackTrace), cause)

open class RecoverableException(status: HttpStatus, detail: String, uri: URI? = null, cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, "title", detail, uri), cause)

private fun problemDetail(status: HttpStatus,
                          title: String?,
                          detail: String? = "",
                          uri: URI?,
                          stackTrace: String? = null) =
    forStatusAndDetail(status, detail).apply {
        this.title = title
        setProperty("uri", uri)
        stackTrace?.let { setProperty("stackTrace", it) }
    }

data class ErrorResponse(val error: String,
                         val validationErrors: List<String>? = emptyList(),
                         val stackTrace: String? = null)
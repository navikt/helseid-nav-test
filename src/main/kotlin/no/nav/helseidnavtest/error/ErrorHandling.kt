package no.nav.helseidnavtest.error

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
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
            BAD_REQUEST, NOT_FOUND -> IrrecoverableException(res.statusCode as HttpStatus, req.uri, respons)
            else -> RecoverableException(res.statusCode as HttpStatus, "Fikk response ${res.statusCode}", req.uri)
        }
    }
}

fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String): Nothing =
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException(detail = detail, uri = req.uri)
        else -> RecoverableException(res.statusCode as HttpStatus, "Fikk response ${res.statusCode}", req.uri)
    }

class NotFoundException(detail: String? = null,
                        uri: URI,
                        stackTrace: String? = null,
                        cause: Throwable? = null) :
    IrrecoverableException(NOT_FOUND, uri, detail, stackTrace, emptyList(), cause)

open class IrrecoverableException(status: HttpStatus,
                                  uri: URI,
                                  detail: String? = null,
                                  stackTrace: String? = null,
                                  validationErrors: List<String> = emptyList(),
                                  cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, detail, uri, stackTrace, validationErrors), cause) {
    constructor(status: HttpStatus, uri: URI, errorResponse: ErrorResponse, cause: Throwable? = null) :
            this(status,
                uri,
                errorResponse.error,
                errorResponse.stackTrace,
                errorResponse.validationErrors,
                cause)
}

open class RecoverableException(status: HttpStatus, detail: String, uri: URI, cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, detail, uri), cause)

private fun problemDetail(status: HttpStatus,
                          detail: String?,
                          uri: URI,
                          stackTrace: String? = null,
                          validationErrors: List<String> = emptyList()) =
    forStatusAndDetail(status, detail).apply {
        this.title = status.reasonPhrase
        type = uri
        validationErrors.isNotEmpty().let { setProperty("validationErrors", validationErrors) }
        stackTrace?.let { setProperty("stackTrace", it) }
    }

data class ErrorResponse(val error: String,
                         val validationErrors: List<String> = emptyList(),
                         val stackTrace: String? = null)
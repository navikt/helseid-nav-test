package no.nav.helseidnavtest.error

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail.forStatusAndDetail
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.ErrorResponseException
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler
import java.net.URI

@Component
@Qualifier(EDI20)
class BodyConsumingErrorHandler(private val m: ObjectMapper) : ErrorHandler {
    private val log = getLogger(BodyConsumingErrorHandler::class.java)

    override fun handle(req: HttpRequest, res: ClientHttpResponse) {
        log.info("Handling error response ${res.statusCode} from ${req.uri}")
        throw when (val code = res.statusCode as HttpStatus) {
            BAD_REQUEST, NOT_FOUND -> IrrecoverableException(code, req.uri, m.readValue<ErrorResponse>(res.body)).also {
                log.warn("Irrecoverable request $it")
            }

            else -> RecoverableException(res.statusCode as HttpStatus, req.uri)
        }
    }
}

@Component
@Primary
class DefaultErrorHandler : ErrorHandler {
    override fun handle(req: HttpRequest, res: ClientHttpResponse) {
        throw when (val code = res.statusCode as HttpStatus) {
            BAD_REQUEST, NOT_FOUND -> IrrecoverableException(code, req.uri)
            else -> RecoverableException(res.statusCode as HttpStatus, req.uri)
        }
    }
}

open class IrrecoverableException(status: HttpStatusCode,
                                  uri: URI,
                                  detail: String? = null,
                                  cause: Throwable? = null,
                                  stackTrace: String? = null,
                                  validationErrors: List<String>? = emptyList()) :
    ErrorResponseException(status, problemDetail(status, detail, uri, stackTrace, validationErrors), cause) {
    constructor(status: HttpStatusCode, uri: URI, errors: ErrorResponse, cause: Throwable? = null) :
            this(status,
                uri,
                errors.error,
                cause,
                errors.stackTrace,
                errors.validationErrors)

    class NotFoundException(uri: URI,
                            detail: String? = NOT_FOUND.reasonPhrase,
                            cause: Throwable? = null,
                            stackTrace: String? = null) :
        IrrecoverableException(NOT_FOUND, uri, detail, cause, stackTrace, emptyList())

    override fun toString() = "IrrecoverableException(status=${statusCode}, detail=$body"
}

open class RecoverableException(status: HttpStatusCode,
                                uri: URI,
                                detail: String? = "Fikk respons $status",
                                cause: Throwable? = null) :
    ErrorResponseException(status, problemDetail(status, detail, uri), cause)

private fun problemDetail(status: HttpStatusCode,
                          detail: String?,
                          uri: URI,
                          stackTrace: String? = null,
                          validationErrors: List<String>? = emptyList()) =
    forStatusAndDetail(status, detail).apply {
        title = HttpStatus.resolve(status.value())?.reasonPhrase ?: status.toString()
        validationErrors?.isNotEmpty().let { setProperty("validationErrors", validationErrors) }
        stackTrace?.let { setProperty("stackTrace", it) }
    }

data class ErrorResponse(val error: String,
                         val validationErrors: List<String>? = emptyList(),
                         val stackTrace: String? = null)
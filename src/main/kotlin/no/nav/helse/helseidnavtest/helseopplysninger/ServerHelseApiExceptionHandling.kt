package no.nav.helse.helseidnavtest.helseopplysninger

import com.fasterxml.jackson.databind.DatabindException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ServerHelseApiExceptionHandling : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(AccessDeniedException::class)
    fun auth(e : RuntimeException, req : NativeWebRequest) = createProblem(e, req, UNAUTHORIZED)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e : Exception, req : NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(NotFound::class)
    fun ikkeFunnet(e : NotFound, req : NativeWebRequest) = createProblem(e, req, NOT_FOUND)

    @ExceptionHandler(HttpMessageConversionException::class)
    fun messageConversion(e : HttpMessageConversionException, req : NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(Exception::class)
    fun catchAll(e : Exception, req : NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    private fun createProblem(e : Exception, req : NativeWebRequest, status : HttpStatus, substatus : Substatus? = null) =
        ResponseEntity.status(status)
            .headers(HttpHeaders().apply { contentType = MediaType.APPLICATION_PROBLEM_JSON })
            .body(createProblemDetail(e, status, e.message ?: e.javaClass.simpleName, substatus?.name, null, req).apply {
             //  setProperty(NAV_CALL_ID, callId())
                substatus?.let { setProperty(SUBSTATUS, it) }
            }.also { log(e, it, req, status) })

    private fun log(t : Throwable, problem : ProblemDetail, req : NativeWebRequest, status : HttpStatus) =
        if (status in listOf(UNPROCESSABLE_ENTITY, UNSUPPORTED_MEDIA_TYPE)) {
            logWarning(req, problem, status, t)
        }
        else {
            logError(req, problem, status, t)
        }

    private fun logError(req : NativeWebRequest, problem : ProblemDetail, status : HttpStatus, t : Throwable) =
        log.error("$req $problem ${status.reasonPhrase}: ${t.message}", t)

    private fun logWarning(req : NativeWebRequest, problem : ProblemDetail, status : HttpStatus, t : Throwable) =
        log.warn("$req $problem ${status.reasonPhrase}: ${t.message}", t)

    companion object {

        private const val SUBSTATUS = "substatus"
    }

    private enum class Substatus { UNSUPPORTED }
}
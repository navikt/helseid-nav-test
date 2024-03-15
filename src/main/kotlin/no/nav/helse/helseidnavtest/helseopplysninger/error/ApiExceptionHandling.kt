package no.nav.helse.helseidnavtest.helseopplysninger.error

import com.fasterxml.jackson.databind.DatabindException
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.OrganisasjonRestClientAdapter
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.OrganisasjonRestClientAdapter.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.*
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity.*
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.slf4j.LoggerFactory.getLogger

@ControllerAdvice
class ApiExceptionHandling : ResponseEntityExceptionHandler() {
    private val log = getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(OrganisasjonException::class)
    fun ikkeFunnet(e: OrganisasjonException, req: NativeWebRequest) = createProblem(e, req, NOT_FOUND)

    @ExceptionHandler(HttpMessageConversionException::class)
    fun messageConversion(e: HttpMessageConversionException, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    private fun createProblem(e: Exception, req: NativeWebRequest, status: HttpStatus) =
        status(status)
            .headers(HttpHeaders().apply { contentType = APPLICATION_PROBLEM_JSON })
            .body(createProblemDetail(e, status, e.message ?: e.javaClass.simpleName,null, null, req).apply {
              //  setProperty(NAV_CALL_ID, callId())
            }.also { log(e, it, req, status) })

    private fun log(t: Throwable, problem: ProblemDetail, req: NativeWebRequest, status: HttpStatus) {
        if(status == UNPROCESSABLE_ENTITY ||  status == UNSUPPORTED_MEDIA_TYPE){
            logWarning(req, problem, status, t)
        }
        else {
            logError(req, problem, status, t)
        }
    }

    private fun logError(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus, t: Throwable) {
        log.error("$req $problem ${status.reasonPhrase}: ${t.message}", t)
    }

    private fun logWarning(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus, t: Throwable) =
        log.warn("$req $problem ${status.reasonPhrase}: ${t.message}", t)



    companion object {
        private const val SUBSTATUS = "substatus"
    }

}

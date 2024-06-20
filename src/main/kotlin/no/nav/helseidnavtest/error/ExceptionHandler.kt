package no.nav.helseidnavtest.error

import com.fasterxml.jackson.databind.DatabindException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.annotation.Order
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.ResponseEntity.status
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
@Order(-1)
class ExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest, headers: HttpHeaders) = createProblem(e, req, BAD_REQUEST,headers)


    @ExceptionHandler(AccessDeniedException::class)
    fun accessDenied(e: AccessDeniedException, req: NativeWebRequest, headers: HttpHeaders) = createProblem(e, req, UNAUTHORIZED,headers)

    @ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest, headers: HttpHeaders) = createProblem(e, req, BAD_REQUEST,headers)

    private fun createProblem(e: Exception, req: NativeWebRequest, status: HttpStatus, headers: HttpHeaders) =
        status(status)
            .headers(HttpHeaders().apply { contentType = APPLICATION_PROBLEM_JSON })
            .body(createProblemDetail(e, status, e.message ?: e.javaClass.simpleName,null, null, req).apply {
            }.also { log(e, it, req, status, headers) })

    private fun log(t: Throwable, problem: ProblemDetail, req: NativeWebRequest, status: HttpStatus, headers: HttpHeaders) {
        if(status in listOf(UNPROCESSABLE_ENTITY,UNSUPPORTED_MEDIA_TYPE)) {
            logWarning(req, problem, status, headers,t)
        }
        else {
            logError(req, problem, status, headers,t)
        }
    }

    private fun logError(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus, headers: HttpHeaders, t: Throwable) {
        log.error("OOPS $req $headers $problem ${status.reasonPhrase}: ${t.message}", t)
    }

    private fun logWarning(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus,headers: HttpHeaders, t: Throwable) =
        log.warn("OOPS $req $headers, $problem ${status.reasonPhrase}: ${t.message}", t)
}

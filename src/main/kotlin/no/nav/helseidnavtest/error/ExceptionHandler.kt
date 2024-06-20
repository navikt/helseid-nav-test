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
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
@Order(-1)
class ExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = getLogger(javaClass)

    //@ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)


    //@ExceptionHandler(AccessDeniedException::class)
    fun accessDenied(e: AccessDeniedException, req: NativeWebRequest) = createProblem(e, req, UNAUTHORIZED)

    //@ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    private fun createProblem(e: Exception, req: NativeWebRequest, status: HttpStatus) =
        status(status)
            .headers(HttpHeaders().apply { contentType = APPLICATION_PROBLEM_JSON })
            .body(createProblemDetail(e, status, e.message ?: e.javaClass.simpleName,null, null, req).apply {
            }.also { log(e, it, req, status) })

    private fun log(t: Throwable, problem: ProblemDetail, req: NativeWebRequest, status: HttpStatus) =
        if(status in listOf(UNPROCESSABLE_ENTITY,UNSUPPORTED_MEDIA_TYPE)) {
            logWarning(req, problem, status, t)
        }
        else {
            logError(req, problem, status, t)
        }

    private fun logError(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus, t: Throwable) = log.error("OOPS $req $problem ${status.reasonPhrase}: ${t.message}", t)

    private fun logWarning(req: NativeWebRequest, problem: ProblemDetail, status: HttpStatus, t: Throwable) = log.warn("OOPS $req $problem ${status.reasonPhrase}: ${t.message}", t)
}

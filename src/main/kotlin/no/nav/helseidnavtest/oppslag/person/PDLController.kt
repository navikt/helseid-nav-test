package no.nav.helseidnavtest.oppslag.person
import no.nav.helseidnavtest.error.IrrecoverableGraphQLException.NotFoundGraphQLException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.ResponseEntity.status
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.NativeWebRequest

@RestController(PDL)
class PDLController(private val pdl: PDLClient) {

    @GetMapping("/ping") fun ping() = pdl.ping()

    @GetMapping("/$PDL") fun navn(@RequestParam fnr: Fødselsnummer) = pdl.navn(fnr)

    @ExceptionHandler(NotFoundGraphQLException::class)
    fun notFound(e: NotFoundGraphQLException) = createProblem(e, NOT_FOUND)}

private fun createProblem(e: ErrorResponseException, status: HttpStatus) =
    status(status)
        .headers(HttpHeaders().apply { contentType = APPLICATION_PROBLEM_JSON })
        .body(e.body)


package no.nav.helseidnavtest.oppslag.person
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.ResponseEntity.status
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(PDL)
class PDLController(private val pdl: PDLClient) {

    protected val log = getLogger(PDLController::class.java)

    @GetMapping("/ping") fun ping() = pdl.ping()

    @GetMapping("/$PDL") fun navn(@RequestParam fnr: Fødselsnummer) = pdl.navn(fnr)

    @ExceptionHandler(ErrorResponseException::class)
    fun problem(e: ErrorResponseException) = createProblem(e)

private fun createProblem(e: ErrorResponseException) =
    status(e.statusCode)
        .headers(e.headers.apply { contentType = APPLICATION_PROBLEM_JSON })
        .body(e.body)

}
package no.nav.helseidnavtest.edi20
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MOTTAKER_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.SENDER_ID
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(EDI20)
class EDI20Controller(private val a: EDI20Service) {

    private val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages(@Parameter(schema = Schema(allowableValues = arrayOf(SENDER_ID, MOTTAKER_ID))) @RequestParam herId: String) = a.poll(HerId(herId))

    @GetMapping("/dialogmelding") fun dialogmelding(@Parameter(schema = Schema(allowableValues = arrayOf(SENDER_ID, MOTTAKER_ID))) @RequestParam herId: String): String {
        runCatching {
            a.send(HerId(herId))
            return "OK"
        }.getOrElse {
            log.error("Feil ved generering av dialogmelding", it)
            return "NOT OK"
        }
    }
}

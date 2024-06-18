package no.nav.helseidnavtest.edi20
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(EDI20)
class EDI20Controller(private val a: EDI20RestClientAdapter) {

    protected val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages() = a.messages()



}
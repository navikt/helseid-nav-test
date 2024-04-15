package no.nav.helseidnavtest.oppslag.person
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(PDL)
class PDLController(private val pdl: PDLClient) {

    @GetMapping("/ping") fun ping() = pdl.ping()

    @GetMapping("/$PDL") fun navn(@RequestParam fnr: Fødselsnummer) = pdl.navn(fnr)
}
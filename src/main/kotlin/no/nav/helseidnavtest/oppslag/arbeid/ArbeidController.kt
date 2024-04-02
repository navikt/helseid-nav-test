package no.nav.helseidnavtest.oppslag.arbeid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("arbeid")
class ArbeidController(private val arbeid: ArbeidClient) {


    @GetMapping("/arbeid") fun arbeid(@RequestParam fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr)


}
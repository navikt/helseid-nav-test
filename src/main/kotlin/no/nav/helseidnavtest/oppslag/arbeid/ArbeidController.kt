package no.nav.helseidnavtest.oppslag.arbeid
import no.nav.helseidnavtest.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(ARBEID)
class ArbeidController(private val arbeid: ArbeidClient) {


    @GetMapping("/$ARBEID") fun arbeid(@RequestParam fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr)


}
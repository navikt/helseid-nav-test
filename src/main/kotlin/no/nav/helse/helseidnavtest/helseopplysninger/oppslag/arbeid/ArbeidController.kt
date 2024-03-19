package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController("arbeid")
class ArbeidController(private val arbeid: ArbeidClient) {


    @GetMapping("/arbeid") fun arbeid(@RequestParam fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr)


}
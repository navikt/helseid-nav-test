package no.nav.helseidnavtest.oppslag.person
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

class PDLClient(private val pdl: PDLWebClientAdapter) {
    fun ping() = pdl.ping()
    fun navn(fnr: Fødselsnummer) = pdl.person(fnr).navn
}
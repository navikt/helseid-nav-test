package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/fastlege")
class FastlegeController(private val adapter: FastlegeWSAdapter) {

    @GetMapping("/kontor")
    fun kontor(@RequestParam fnr: Fødselsnummer) = adapter.kontor(fnr)

    @GetMapping("/bekreft")
    fun bekreftFastlege(@RequestParam hprId: Int, @RequestParam fnr: Fødselsnummer) = adapter.bekreftFastlege(hprId, fnr)
}
package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid.Fødselsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/fastlege")
class FastlegeController(private val adapter: FastlegeWSAdapter) {

    @GetMapping("/finn")
    fun finnFastlege(@RequestParam fnr: Fødselsnummer) = adapter.fastlegeForPasient(fnr)

    @GetMapping("/detaljer")
    fun detaljer(@RequestParam fnr: Fødselsnummer) = adapter.detaljer(fnr)

    @GetMapping("/bekreft")
    fun bekreftFastlege(@RequestParam hprId: Int, @RequestParam fnr: Fødselsnummer) = adapter.bekreftFastlege(hprId, fnr)
}

  //  fastlege(7125186  , "19087999648")

package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.AvtaleId
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("fastlege")
class FastlegeController(private val client: FastlegeClient) {

    @GetMapping("/herid")
    fun herIdForLege(@RequestParam pasient: Fødselsnummer) = client.herIdForLegeViaPasient(pasient)

    @GetMapping("/lege")
    fun lege(@RequestParam pasient: Fødselsnummer) = client.lege(pasient)

    @GetMapping("/legefnr")
    fun legeFnr(@RequestParam navn: String) = client.legeFNR(navn)

    @GetMapping("/avtale")
    fun legeFnr(@RequestParam id: AvtaleId) = client.pasienterForAvtale(id)
}
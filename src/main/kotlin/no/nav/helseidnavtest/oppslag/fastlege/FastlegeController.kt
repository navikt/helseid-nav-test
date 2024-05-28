package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/fastlege")
class FastlegeController(private val client: FastlegeClient) {

    @GetMapping("/kontor")
    fun fastlegeKontorForPasient(@RequestParam pasient: Fødselsnummer) = client.kontorForPasient(pasient)

    @GetMapping("/herid")
    fun herIdForLege(@RequestParam pasient: Fødselsnummer) = client.herIdForLegeViaPasient(pasient)

    @GetMapping("/lege")
    fun lege(@RequestParam pasient: Fødselsnummer) = client.lege(pasient)

}
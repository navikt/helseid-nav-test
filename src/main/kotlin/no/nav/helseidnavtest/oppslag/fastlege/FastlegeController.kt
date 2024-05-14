package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/fastlege")
class FastlegeController(private val client: FastlegeClient) {

    @GetMapping("/kontor")
    fun kontor(@RequestParam fnr: Fødselsnummer) = client.kontor(fnr)

    @GetMapping("/herid")
    fun herid(@RequestParam pasient: Fødselsnummer) = client.herId(pasient)
  }
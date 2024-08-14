package no.nav.helseidnavtest.dialogmelding

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender) {

    @GetMapping(value = ["/send"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.send(pasient)
}
package no.nav.helseidnavtest.dialogmelding

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender, private val generator: DialogmeldingGenerator) {

    @GetMapping(value = ["/generer"])
    fun generer(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient, UUID.randomUUID())

    @GetMapping(value = ["/send"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.send(pasient)
}
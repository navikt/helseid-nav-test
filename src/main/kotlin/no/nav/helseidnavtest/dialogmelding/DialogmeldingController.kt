package no.nav.helseidnavtest.dialogmelding
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender, val generator: DialogmeldingGenerator) {

    @GetMapping(value = ["/generer"])
    fun generer(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient)

    @GetMapping(value = ["/send"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.send(pasient)
}
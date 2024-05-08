package no.nav.helseidnavtest.dialogmelding
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender, private val generator: DialogmeldingGenerator, private val emottak: DialogmeldingRestAdapter) {

    @GetMapping(value = ["/generer"])
    fun generer(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient)

    @GetMapping(value = ["/send"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.send(pasient)

    @GetMapping(value = ["/partner"])
    fun partner(@RequestParam herId: String) = emottak.partnerId(herId)
}
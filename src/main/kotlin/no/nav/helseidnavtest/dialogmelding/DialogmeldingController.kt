package no.nav.helseidnavtest.dialogmelding
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender, val generator: DialogmeldingGenerator) {


    @GetMapping(value = ["/melding"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.sendDialogmelding(generator.genererDialogmelding(pasient))
}
package no.nav.helseidnavtest.dialogmelding
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val generator: DialogmeldingGenerator) {
    

    @GetMapping(value = ["/melding"])
    fun xml(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient)
}
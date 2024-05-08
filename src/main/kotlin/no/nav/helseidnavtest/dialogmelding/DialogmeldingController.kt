package no.nav.helseidnavtest.dialogmelding
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val sender: DialogmeldingSender, private val generator: DialogmeldingGenerator, private val emottak: EmottakWSAdapter) {

    @GetMapping(value = ["/generer"])
    fun generer(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient)

    @GetMapping(value = ["/send"])
    fun send(@RequestParam pasient: Fødselsnummer) = sender.send(pasient)

    @GetMapping(value = ["/partner"])
    fun partner(@RequestParam orgnr: String, @RequestParam herId: String) = emottak.partnerRef(orgnr, herId)
}
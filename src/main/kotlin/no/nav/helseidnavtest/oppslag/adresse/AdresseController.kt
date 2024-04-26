package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("adresse")
class AdresseController(private val adresse: AdresseWSAdapter) {
    @GetMapping("/idforkontor") fun search(@RequestParam orgnr: Virksomhetsnummer) = adresse.herIdForKontor(orgnr)
    @GetMapping("/idforhpr") fun search(@RequestParam hpr: Int) = adresse.herIdForHpr(hpr)
}
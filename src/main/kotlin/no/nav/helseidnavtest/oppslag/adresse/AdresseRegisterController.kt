package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.dialogmelding.Orgnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("adresse")
class AdresseRegisterController(private val adresse: AdresseRegisterClient) {
    @GetMapping("/idforkontor") fun search(@RequestParam orgnr: Orgnummer) = adresse.herIdForVirksomhet(orgnr)
}
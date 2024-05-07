package no.nav.helseidnavtest.oppslag.adresse
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("adresse")
class AdresseRegisterController(private val adresse: AdresseRegisterClient) {
    @GetMapping("/idforkontor") fun search(@RequestParam orgnr: Virksomhetsnummer) = adresse.herIdForVirksomhet(orgnr)
    @GetMapping("/idforhpr") fun search(@RequestParam hpr: HprId) = adresse.herIdForHpr(hpr)
}
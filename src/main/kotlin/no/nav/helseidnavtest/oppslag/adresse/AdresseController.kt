package no.nav.helseidnavtest.oppslag.adresse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("adresse")
class AdresseController(private val adresse: AdresseWSAdapter) {
    @GetMapping("/details") fun detaila(@RequestParam herId: Int) = adresse.details(herId)

}
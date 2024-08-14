package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("adresse")
class AdresseRegisterController(private val adresse: AdresseRegisterClient) {

    @GetMapping("/part")
    fun kommunikasjonsPart(@RequestParam id: HerId) = adresse.kommunikasjonsPart(id)

}
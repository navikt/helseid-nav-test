package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.Companion.oidcUser
import org.springframework.web.bind.annotation.RequestParam

@RestController("arbeid")
class ArbeidController(private val adapter: OrganisasjonRestClientAdapter) {


    @GetMapping("/navn") fun test(@RequestParam orgnr: OrgNummer) = adapter.orgNavn(orgnr)

}
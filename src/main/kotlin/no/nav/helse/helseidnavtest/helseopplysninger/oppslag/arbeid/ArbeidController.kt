package no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid

import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon.OrgNummer
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.organisasjon.OrganisasjonRestClientAdapter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

@RestController("arbeid")
class ArbeidController(private val adapter: OrganisasjonRestClientAdapter, private val arbeid: ArbeidClient) {


    @GetMapping("/navn") fun navn(@RequestParam orgnr: OrgNummer) = adapter.orgNavn(orgnr)

    @GetMapping("/arbeid") fun arbeid(@RequestParam fnr: Fødselsnummer) = arbeid.arbeidInfo(fnr)


}
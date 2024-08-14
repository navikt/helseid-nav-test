package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Orgnummer
import no.nav.helseidnavtest.edi20.EDI20DialogmeldingGenerator.*
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.*
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {
    
    fun kommunikasjonsParter(fra: HerId, til: HerId) =
        KommunikasjonsParter(kommunikasjonsPart(fra), kommunikasjonsPart(til))

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi)

}
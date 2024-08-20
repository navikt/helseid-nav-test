package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.HprId
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    fun tjenester(fra: HerId, til: HerId) =
        Tjenester(kommunikasjonsPart(fra) as Tjeneste, kommunikasjonsPart(til) as Tjeneste)

    fun lege(id: HprId, org: HerId) = adapter.lagLege(id, org)

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi)

}
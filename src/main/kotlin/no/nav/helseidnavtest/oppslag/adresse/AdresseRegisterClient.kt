package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.other
import no.nav.helseidnavtest.oppslag.adresse.Innsending.Parter
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Tjeneste
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    fun parter(fra: HerId, til: HerId = fra.other(), helsePersonell: OidcUser) =
        Parter(kommunikasjonsPart(fra) as Tjeneste,
            KommunikasjonsPart.Mottaker(kommunikasjonsPart(til), helsePersonell))

    fun kommunikasjonsPart(herId: HerId) = adapter.kommunikasjonsPart(herId.verdi.toInt())

}
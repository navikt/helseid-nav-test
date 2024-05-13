package no.nav.helseidnavtest.dialogmelding

import org.springframework.stereotype.Component

@Component
class DialogmeldingClient(private val adapter: DialogmeldingRestAdapter) {
    fun partnerId(herId: HerId, behandlerKontor: BehandlerKontor) = PartnerId(adapter.partnerId(herId.verdi,behandlerKontor))
}
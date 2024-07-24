package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.error.RecoverableException
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
@Retryable(include = [RecoverableException::class])
class DialogmeldingClient(private val adapter: DialogmeldingRestAdapter) {
    fun partnerId(herId: HerId, behandlerKontor: BehandlerKontor) =
        PartnerId(adapter.partnerId(herId.verdi, behandlerKontor))
}
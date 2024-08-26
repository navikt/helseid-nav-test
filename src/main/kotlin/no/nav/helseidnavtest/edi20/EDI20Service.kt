package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(private val adapter: EDI20RestClientAdapter,
                   private val recoverer: Recoverer) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)

    @Retryable
    fun send(bestilling: Bestilling) = adapter.send(bestilling)

    @Retryable(recover = "send")
    fun send(e: Throwable, bestilling: Bestilling) = recoverer.recover(bestilling)
    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)

    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

}
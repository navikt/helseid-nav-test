package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(private val adapter: EDI20RestClientAdapter,
                   private val recoverer: KafkaRecoverer) {

    private val log = getLogger(EDI20Service::class.java)

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)

    @Retryable(listeners = ["loggingRetryListener"])
    fun send(bestilling: Bestilling): Nothing = throw IllegalStateException("OOPS") // adapter.send(bestilling)

    @Recover
    fun send(e: Throwable, bestilling: Bestilling): String {
        log.info("Recovering bestilling: $bestilling from exception", e)
        recoverer.recover(bestilling)
        return "OK"  // Must return something
    }

    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)

    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

}
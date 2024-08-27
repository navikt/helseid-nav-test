package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(private val adapter: EDI20RestClientAdapter,
                   private val recoverer: KafkaRecoverer) {

    private val log = getLogger(EDI20Service::class.java)

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)

    fun send(bestilling: Bestilling) = adapter.send(bestilling)

    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)

    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

}
package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(private val generator: EDI20DialogmeldingGenerator,
                   private val adapter: EDI20RestClientAdapter) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)

    fun send(bestilling: Bestilling) = adapter.send(bestilling)

    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)

    fun lesOgAckAlle(herId: HerId) =
        mapOf(herId to (adapter.poll(herId, true)
            ?.flatMap { m ->
                m.messageIds.map {
                    konsumert(m.herId, it)
                    //  apprec(m.herId, it)
                    it
                }
            } ?: emptyList()))

    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)
    
}
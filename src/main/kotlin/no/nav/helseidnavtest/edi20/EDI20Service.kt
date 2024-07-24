package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(val adapter: EDI20RestClientAdapter) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId.verdi, id)
    fun les(herId: HerId, id: UUID) = adapter.les(herId.verdi, id)
    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId.verdi, appRec)
    fun send(herId: HerId) = adapter.send(herId.verdi)
    fun lest(herId: HerId, id: UUID) = adapter.lest(herId.verdi, id)
    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId.verdi, id)

}
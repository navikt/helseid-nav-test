package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EDI20Service(val a: EDI20RestClientAdapter, val b: EDI20Config) {

    fun status(id: UUID, herId: HerId) = a.status(id,herId)
    fun les(id: UUID, herId: HerId) = a.les(id,herId)
    fun poll(herId: HerId, appRec: Boolean) = a.poll(herId, appRec)
    fun send(herId: HerId) = a.send(herId)
    fun lest(id: UUID, herId: HerId) = a.lest(id, herId)
}
package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EDI20Service(val a: EDI20RestClientAdapter, val b: EDI20Config) {

    fun status(uuid: UUID,herId: HerId) = a.status(uuid,herId)
    fun hent(uuid: UUID,herId: HerId) = a.hent(uuid,herId)
    fun poll(herId: HerId) = a.poll(herId)
    fun send(herId: HerId) = a.send(herId)
    fun markRead(id: UUID, herId: HerId) = a.markRead(id, herId)
}
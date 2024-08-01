package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class EDI20Service(val adapter: EDI20RestClientAdapter) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)
    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)
    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)
    fun send(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) = adapter.send(herId, pasient, vedlegg)
    fun lest(herId: HerId, id: UUID) = adapter.lest(herId, id)
    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

}
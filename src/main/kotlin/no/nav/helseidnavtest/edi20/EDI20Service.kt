package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.*

@Service
class EDI20Service(private val generator: EDI20DialogmeldingGenerator,
                   private val deft: EDI20DeftService,
                   private val adapter: EDI20RestClientAdapter) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun poll(herId: HerId, appRec: Boolean) = adapter.poll(herId, appRec)

    fun sendRef(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        vedlegg?.let {
            adapter.send(fra, hodemelding(fra, til, pasient, Pair(deft.upload(fra, it), it.contentType!!)))
        } ?: adapter.send(fra, hodemelding(fra, til, pasient))

    fun sendInline(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        adapter.send(fra, hodemelding(fra, til, pasient, vedlegg))

    fun showRef(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile) =
        hodemelding(fra, til, pasient, Pair(deft.upload(fra, vedlegg), vedlegg.contentType!!))

    fun showInline(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        hodemelding(fra, til, pasient, vedlegg)

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

    private fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>? = null) =
        generator.hodemelding(fra, til, pasient, vedlegg)

    private fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        generator.hodemelding(fra, til, pasient, vedlegg)

}
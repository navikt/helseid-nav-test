package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.log
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

    fun sendRef(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        vedlegg?.let {
            adapter.send(herId, hodemelding(herId, pasient, Pair(deft.upload(herId, it), it.contentType!!)))
        } ?: adapter.send(herId, hodemelding(herId, pasient))

    fun sendInline(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        adapter.send(herId, hodemelding(herId, pasient, vedlegg))

    fun showRef(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile) =
        hodemelding(herId, pasient, Pair(deft.upload(herId, vedlegg), vedlegg.contentType!!))

    fun showInline(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        hodemelding(herId, pasient, vedlegg)

    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)
    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

    private fun hodemelding(fra: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>? = null) =
        generator.hodemelding(fra, fra.other(), pasient, vedlegg).also { log.info("Hodemelding er $it") }

    private fun hodemelding(fra: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        generator.hodemelding(fra, fra.other(), pasient, vedlegg).also { log.info("Hodemelding er $it") }

}
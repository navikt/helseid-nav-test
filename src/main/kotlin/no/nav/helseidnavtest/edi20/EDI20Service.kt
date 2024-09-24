package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.PollParameters
import no.nav.helseidnavtest.oppslag.adresse.Innsending
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.util.*

@Service
class EDI20Service(private val adapter: EDI20RestClientAdapter) {

    fun status(herId: HerId, id: UUID) = adapter.status(herId, id)

    fun les(herId: HerId, id: UUID) = adapter.les(herId, id)

    fun raw(herId: HerId, id: UUID) = adapter.raw(herId, id)

    fun poll(params: PollParameters) = adapter.poll(params)

    @PreAuthorize("hasAuthority('LE_4')")
    fun send(innsending: Innsending) = adapter.send(innsending)

    fun konsumert(herId: HerId, id: UUID) = adapter.konsumert(herId, id)

    fun apprec(herId: HerId, id: UUID) = adapter.apprec(herId, id)

    val uri = adapter.uri

}
package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service

@Service
class EDI20Service(val a: EDI20RestClientAdapter, val b: EDI20Config) {

    fun poll(herId: HerId) = a.poll(herId)
    fun send(pasient: Fødselsnummer) = a.send(pasient)
}
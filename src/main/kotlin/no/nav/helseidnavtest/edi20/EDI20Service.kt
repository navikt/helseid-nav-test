package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.springframework.stereotype.Service

@Service
class EDI20Service(val a: EDI20RestClientAdapter, val b: EDI20Config) {

    fun poll() = a.messages()
    fun send(pasient: Fødselsnummer) = a.send(pasient)
}
package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import org.springframework.stereotype.Service

@Service
class FastlegeClient(private val adapter: FastlegeWSAdapter) {
    fun kontor(fnr: Fødselsnummer) = adapter.kontor(fnr)
}
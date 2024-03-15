package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import org.slf4j.LoggerFactory.getLogger

data class OrgNummer(@get:JsonValue val orgnr : String) {

    protected val log = getLogger(javaClass)

    init {
        if (currentCluster() == PROD_GCP) {
            log.trace("Vi er i cluster {}, gjør validering av {}", currentCluster(), orgnr)
            require(orgnr.length == 9) { "Orgnr må ha lengde 9, $orgnr har lengde ${orgnr.length} " }
            require(orgnr.startsWith("8") || orgnr.startsWith("9")) { "Orgnr må begynne med 8 eller 9" }
            require(orgnr[8].code - 48 == mod11(orgnr.substring(0, 8))) { "${orgnr[8]} feilet mod11 validering" }
        }
        else {
            log.trace("Vi er i cluster {}, ingen validering av {}", currentCluster(), orgnr)
        }
    }

    companion object {

        private val WEIGHTS = intArrayOf(3, 2, 7, 6, 5, 4, 3, 2)

        private fun mod11(orgnr: String): Int {
            val weightedSum = orgnr.indices.sumOf { (orgnr[it].code - 48) * WEIGHTS[it] }
            val remainder = 11 - weightedSum % 11
            return if (remainder == 11) 0 else remainder
        }
    }
}
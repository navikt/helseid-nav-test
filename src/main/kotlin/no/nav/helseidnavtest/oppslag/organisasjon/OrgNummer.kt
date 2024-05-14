package no.nav.helseidnavtest.oppslag.organisasjon

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.Cluster.PROD_GCP
import org.slf4j.LoggerFactory.getLogger

data class OrgNummer(@get:JsonValue val verdi : String) {

    private val log = getLogger(javaClass)

    init {
        if (currentCluster() == PROD_GCP) {
            with(verdi)  {
                require(length == WEIGHTS.size + 1) { "Orgnr må ha lengde ${WEIGHTS.size + 1}, $verdi har lengde ${verdi.length} " }
                require(startsWith("8") || startsWith("9")) { "Orgnr må begynne med 8 eller 9" }
                require(this[8].code - 48 == mod11(substring(0, 8))) { "${this[8]} feilet mod11 validering" }
            }
           }
        else {
            log.trace("Vi er i cluster {}, ingen validering av {}", currentCluster(), verdi)
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
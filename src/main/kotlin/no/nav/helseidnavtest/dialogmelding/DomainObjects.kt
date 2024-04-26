package no.nav.helseidnavtest.dialogmelding

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.boot.conditionals.Cluster.DEV_GCP
import no.nav.helseidnavtest.dialogmelding.BehandlerKategori.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKode.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*


data class Arbeidstaker(
    val arbeidstakerPersonident: Fødselsnummer,
    val fornavn: String = "",
    val mellomnavn: String? = null,
    val etternavn: String = "",
    val mottatt: OffsetDateTime = OffsetDateTime.now(),
    )




data class Dialogmelding(
    val uuid: UUID,
    val behandler: Behandler,
    val arbeidstakerPersonident: Fødselsnummer,
    val conversationUuid: UUID,
    val tekst: String?,
    val vedlegg: ByteArray? = null,
    val type: DialogmeldingType = DIALOG_NOTAT,
    val kodeverk: DialogmeldingKodeverk = HENVENDELSE,
    val kode: DialogmeldingKode = KODE8,
)

enum class DialogmeldingKode(val value: Int) {
    KODE8(8),
}
enum class DialogmeldingKodeverk(val id: String) {
    HENVENDELSE("2.16.578.1.12.4.1.1.8127"),
}

enum class DialogmeldingType {
    DIALOG_NOTAT,
}

data class Behandler(
    val behandlerRef: UUID,
    val personident: Fødselsnummer?,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val herId: Int?,
    val hprId: Int?,
    val telefon: String?,
    val kontor: BehandlerKontor,
    val kategori: BehandlerKategori =  LEGE,
    val mottatt: LocalDateTime = LocalDateTime.now(),
    val invalidated: LocalDateTime? = null,
    val suspendert: Boolean = false,
)

data class BehandlerKontor(
    val partnerId: PartnerId,
    val navn: String?,
    val adresse: String?,
    val postnummer: String?,
    val poststed: String?,
    val orgnummer: Virksomhetsnummer?,
    var herId: Int? = null,
    val mottatt: LocalDateTime = LocalDateTime.now(),
    val system: String = "Helseopplysninger",
    val dialogmeldingEnabled: Boolean = true,
    val dialogmeldingEnabledLocked: Boolean = false,
)

enum class BehandlerKategori(val kategoriKode: String ) {
    LEGE("LE")
}
data class PartnerId(val value: Int) {
    override fun toString() =  value.toString()
}

data class Fødselsnummer(@get:JsonValue val value : String) {
    init {
        require(value.length == 11) { "Fødselsnummer $value er ikke 11 siffer" }
        require(mod11(W1, value) == value[9] - '0') { "Første kontrollsiffer $value[9] ikke validert" }
        require(mod11(W2, value) == value[10] - '0') { "Andre kontrollsiffer $value[10] ikke validert" }
    }
    val type  = if (value[0].digitToInt() > 3) "DNR" else "FNR"

    companion object {

        private val W1 = intArrayOf(2, 5, 4, 9, 8, 1, 6, 7, 3)
        private val W2 = intArrayOf(2, 3, 4, 5, 6, 7, 2, 3, 4, 5)

        private fun mod11(weights : IntArray, fnr : String) =
            with(weights.indices.sumOf { weights[it] * (fnr[(weights.size - 1 - it)] - '0') } % 11) {
                when (this) {
                    0 -> 0
                    1 -> throw IllegalArgumentException(fnr)
                    else -> 11 - this
                }
            }
    }
}

data class Virksomhetsnummer(@get:JsonValue val value : String) {

    constructor(orgnr : Int) : this(orgnr.toString())

    private  val log : Logger = getLogger(javaClass)

    init {
        if (currentCluster() != DEV_GCP) {
            log.trace("Vi er i cluster {}, gjør validering av {}", currentCluster(), value)
            require(value.length == 9) { "Orgnr må ha lengde 9, $value har lengde ${value.length} " }
            require(value.startsWith("8") || value.startsWith("9")) { "Orgnr må begynne med 8 eller 9" }
            require(value[8].code - 48 == mod11(value.substring(0, 8))) { "${value[8]} feilet mod11 validering" }
        }
        else {
            log.trace("Vi er i cluster {}, ingen validering av {}", currentCluster(), value)
        }
    }

    companion object {

        private val WEIGHTS = intArrayOf(3, 2, 7, 6, 5, 4, 3, 2)

        private fun mod11(orgnr : String) =
            with(11 - orgnr.indices.sumOf {
                (orgnr[it].code - 48) * WEIGHTS[it]
            } % 11) {
                if (this == 11) 0 else this
            }

    }
}
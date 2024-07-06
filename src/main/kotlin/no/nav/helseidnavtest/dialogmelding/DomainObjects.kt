package no.nav.helseidnavtest.dialogmelding

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.boot.conditionals.Cluster.*
import no.nav.boot.conditionals.Cluster.Companion.currentCluster
import no.nav.helseidnavtest.dialogmelding.BehandlerKategori.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKode.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.*
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer.Type.*
import no.nav.helseidnavtest.oppslag.person.Person.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.lang.String.format
import java.time.LocalDateTime
import java.time.LocalDateTime.*
import java.time.OffsetDateTime
import java.util.*


data class Arbeidstaker(
    val id: Fødselsnummer,
    val navn: Navn,
    val mottatt: OffsetDateTime = OffsetDateTime.now(),
    )




data class Dialogmelding(
    val uuid: UUID,
    val behandler: Behandler,
    val id: Fødselsnummer,
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
    val navn: Navn,
    val herId: HerId,
    val hprId: HprId,
    val telefon: String?,
    val kontor: BehandlerKontor,
    val kategori: BehandlerKategori =  LEGE,
    val mottatt: LocalDateTime = now()
)

data class BehandlerKontor(
    val navn: String?,
    val adresse: String?,
    val postnummer: Postnummer,
    val poststed: String?,
    val orgnummer: Orgnummer,
    var partnerId: PartnerId? = null,
    var herId: HerId? = null,
    val mottatt: LocalDateTime = now(),
    val system: String = "Helseopplysninger"
)

enum class BehandlerKategori(val kode: String ) {
    LEGE("LE")
}


data class Fødselsnummer(@get:JsonValue val verdi : String) {
    private  val log : Logger = getLogger(javaClass)
    init {
        require(verdi.length == 11) { "Fødselsnummer $verdi er ikke 11 siffer" }
        if (currentCluster() == PROD_GCP) {
            log.trace("Vi er i cluster {}, gjør validering av {}", currentCluster(), verdi)
            require(mod11(W1, verdi) == verdi[9] - '0') { "Første kontrollsiffer $verdi[9] ikke validert" }
            require(mod11(W2, verdi) == verdi[10] - '0') { "Andre kontrollsiffer $verdi[10] ikke validert" }
        }
        else {
            log.trace("Vi er i cluster {}, ingen validering av {}", currentCluster(), verdi)
        }
    }
    val type  = if (verdi[0].digitToInt() > 3) DNR else FNR

    enum class Type(val verdi: String) {
        DNR("D-nummer"), FNR("Fødselsnummer")
    }

    companion object {

        private fun String.partialMask(mask : Char = '*') : String {
            val start = length.div(2)
            return replaceRange(start + 1, length, mask.toString().repeat(length - start - 1))
        }

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
    override fun toString() = "${javaClass.simpleName} [fnr=${verdi.partialMask()}]"

}

@JvmInline
value class PartnerId(val value: String) {
    constructor(verdi: Int) : this("$verdi")
}

@JvmInline
value class HerId(val verdi : String)  {
    constructor(verdi: Int) : this("$verdi")
    companion object {
        val SENDER = HerId
    }
}

@JvmInline
value class AvtaleId(val verdi : Long)  {
    init {
        require(verdi > 0) { "AvtaleId  $verdi må være > 0" }

    }
}
@JvmInline
value class HprId(val verdi : String)  {
    constructor(verdi: Int) : this("$verdi")
}

@JvmInline
value class Postnummer(private val raw : Int) {
    val verdi get() = format("%04d",raw)
}

data class Orgnummer(@get:JsonValue val verdi : String) {

    constructor(orgnr : Int) : this("$orgnr")

    private  val log : Logger = getLogger(javaClass)

    init {
        require(verdi.length == 9) { "Orgnummer må ha lengde 9, $verdi har lengde ${verdi.length} " }
        if (currentCluster() == PROD_GCP) {
            log.trace("Vi er i cluster {}, gjør validering av {}", currentCluster(), verdi)
            require(verdi.first() in listOf('8', '9')) { "ç må begynne med 8 eller 9" }
            require(verdi[8].code - 48 == mod11(verdi.substring(0, 8))) { "Kontrollsiffer ${verdi[8]} ikke validert" }
        }
        else {
            log.trace("Vi er i cluster {}, ingen validering av {}", currentCluster(), verdi)
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
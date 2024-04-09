package no.nav.helseidnavtest.domain

import no.nav.helseidnavtest.domain.BehandlerKategori.*
import no.nav.helseidnavtest.domain.DialogmeldingKode.*
import no.nav.helseidnavtest.domain.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.domain.DialogmeldingType.*
import java.time.OffsetDateTime
import java.util.*


data class Arbeidstaker(
        val arbeidstakerPersonident: Personident,
        val fornavn: String = "",
        val mellomnavn: String? = null,
        val etternavn: String = "",
        val mottatt: OffsetDateTime = OffsetDateTime.now(),
    )
    data class Personident(val value: String) {
        private val elevenDigits = Regex("^\\d{11}\$")
        init {
            if (!elevenDigits.matches(value)) {
                throw IllegalArgumentException("Value is not a valid Personident")
            }
        }
        val type  = if (value[0].digitToInt() > 3) "DNR" else "FNR"
    }



data class DialogmeldingBestilling(
    val uuid: UUID,
    val behandler: Behandler,
    val arbeidstakerPersonident: Personident,
    val parentRef: String?,
    val conversationUuid: UUID,
    val type: DialogmeldingType = DIALOG_NOTAT,
    val kodeverk: DialogmeldingKodeverk? = HENVENDELSE, // må tillate null her siden persisterte bestillinger kan mangle denne verdien
    val kode: DialogmeldingKode = KODE8,
    val tekst: String?,
    val vedlegg: ByteArray? = null,
)

enum class DialogmeldingKode(val value: Int) {
    KODE8(8),
}
enum class DialogmeldingKodeverk(val kodeverkId: String) {
    HENVENDELSE("2.16.578.1.12.4.1.1.8127"),
}

enum class DialogmeldingType {
    DIALOG_NOTAT,
}

data class Behandler(
    val behandlerRef: UUID,
    val personident: Personident?,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val herId: Int?,
    val hprId: Int?,
    val telefon: String?,
    val kontor: BehandlerKontor,
    val kategori: BehandlerKategori =  LEGE,
    val mottatt: OffsetDateTime = OffsetDateTime.now(),
    val invalidated: OffsetDateTime? = null,
    val suspendert: Boolean = false,
)

data class BehandlerKontor(
    val partnerId: PartnerId,
    val herId: Int?,
    val navn: String?,
    val adresse: String?,
    val postnummer: String?,
    val poststed: String?,
    val orgnummer: Virksomhetsnummer?,
    val mottatt: OffsetDateTime = OffsetDateTime.now(),
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
data class Virksomhetsnummer(val value: String) {
    private val nineDigits = Regex("^\\d{9}\$")
    init {
        if (!nineDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid Virksomhetsnummer")
        }
    }
}
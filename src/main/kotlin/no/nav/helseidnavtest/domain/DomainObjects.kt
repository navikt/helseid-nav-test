package no.nav.helseidnavtest.domain

import no.nav.helseidnavtest.util.*
import java.time.OffsetDateTime
import java.util.*


data class Arbeidstaker(
        val arbeidstakerPersonident: Personident,
        val fornavn: String = "",
        val mellomnavn: String? = null,
        val etternavn: String = "",
        val mottatt: OffsetDateTime,
    )
    data class Personident(val value: String) {
        init {
            if (!elevenDigits.matches(value)) {
                throw IllegalArgumentException("Value is not a valid Personident")
            }
        }
        fun isDNR() = this.value[0].digitToInt() > 3
    }

val elevenDigits = Regex("^\\d{11}\$")


data class DialogmeldingToBehandlerBestilling(
    val uuid: UUID,
    val behandler: Behandler,
    val arbeidstakerPersonident: Personident,
    val parentRef: String?,
    val conversationUuid: UUID,
    val type: DialogmeldingType,
    val kodeverk: DialogmeldingKodeverk?, // må tillate null her siden persisterte bestillinger kan mangle denne verdien
    val kode: DialogmeldingKode,
    val tekst: String?,
    val vedlegg: ByteArray? = null,
)

enum class DialogmeldingKode(
    val value: Int
) {
    KODE1(1),
    KODE2(2),
    KODE3(3),
    KODE4(4),
    KODE8(8),
    KODE9(9);
}
enum class DialogmeldingKodeverk(val kodeverkId: String) {
    HENVENDELSE("2.16.578.1.12.4.1.1.8127"),
}

enum class DialogmeldingType() {
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
    val kategori: BehandlerKategori,
    val mottatt: OffsetDateTime,
    val invalidated: OffsetDateTime? = null,
    val suspendert: Boolean,
)

data class BehandlerKontor(
    val partnerId: PartnerId,
    val herId: Int?,
    val navn: String?,
    val adresse: String?,
    val postnummer: String?,
    val poststed: String?,
    val orgnummer: Virksomhetsnummer?,
    val dialogmeldingEnabled: Boolean,
    val dialogmeldingEnabledLocked: Boolean,
    val system: String?,
    val mottatt: OffsetDateTime,
)

enum class BehandlerKategori(
    val kategoriKode: String,
) {
    FYSIOTERAPEUT("FT"),
    KIROPRAKTOR("KI"),
    LEGE("LE"),
    MANUELLTERAPEUT("MT"),
    TANNLEGE("TL");

    companion object {
        fun fromKategoriKode(kategori: String?): BehandlerKategori? =
            values().firstOrNull { it.kategoriKode == kategori }
    }
}
data class PartnerId(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }
}
data class Virksomhetsnummer(val value: String) {
    private val nineDigits = Regex("^\\d{9}\$")

    init {
        if (!nineDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid Virksomhetsnummer")
        }
    }
}
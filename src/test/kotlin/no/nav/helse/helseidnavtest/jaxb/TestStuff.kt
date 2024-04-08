package no.nav.helse.helseidnavtest.jaxb

import no.nav.helseidnavtest.domain.*
import no.nav.helseidnavtest.util.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class TestStuff {
    @Test
    fun stuff() {
        val kontor = BehandlerKontor(
            dialogmeldingEnabled = true,
            dialogmeldingEnabledLocked = false,
            system = "System",
            partnerId = PartnerId(123456789),
            navn = "Kontor",
            orgnummer = Virksomhetsnummer("123456789"),
            postnummer = "1234",
            poststed = "Poststed",
            adresse = "Adresse",
            mottatt = OffsetDateTime.now(),
            herId = 12345678)

        val behandler =  Behandler(UUID.randomUUID(),
            fornavn = "Ole",
            mellomnavn ="Mellomnavn",
            etternavn = "Olsen",
            herId = 123456789,
            hprId = 987654321,
            telefon = "12345678",
            suspendert = false,
            kategori = BehandlerKategori.LEGE,
            personident = Personident("12345678901"),
            mottatt = OffsetDateTime.now(),
            kontor = kontor)
        val b = DialogmeldingToBehandlerBestilling(UUID.randomUUID(),
            behandler,
            Personident("03016536325"),
            "parent ref",
            UUID.randomUUID(),
            DialogmeldingType.DIALOG_NOTAT,
            DialogmeldingKodeverk.HENVENDELSE,
            DialogmeldingKode.KODE8,
            "tekst",
            ByteArray(42),
            )
        val arbeidstaker = Arbeidstaker(
            arbeidstakerPersonident = Personident("03016536325"),
            fornavn = "Ola",
            mellomnavn = "Mellomnavn",
            etternavn = "Olsen",
            mottatt = OffsetDateTime.now()
        )

        val m  =DialogmeldingMapper.opprettDialogmelding(b, arbeidstaker)
       println(m.message)
    }
}
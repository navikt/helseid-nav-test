package no.nav.helse.helseidnavtest.jaxb

import no.nav.helseidnavtest.domain.*
import no.nav.helseidnavtest.domain.DialogmeldingKode.*
import no.nav.helseidnavtest.domain.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.domain.DialogmeldingType.*
import no.nav.helseidnavtest.util.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.time.OffsetDateTime
import java.util.*

class TestStuff {
    @Test
    fun stuff() {
        val kontor = BehandlerKontor(
            partnerId = PartnerId(123456789),
            navn = "Et egekontor",
            orgnummer = Virksomhetsnummer("123456789"),
            postnummer = "1234",
            poststed = "Oslo",
            adresse = "Fyrstikkalleen 1",
            herId = 12345678)

        val behandler =  Behandler(UUID.randomUUID(),
            fornavn = "Ole",
            mellomnavn ="Mellomnavn",
            etternavn = "Olsen",
            herId = 123456789,
            hprId = 987654321,
            telefon = "12345678",
            personident = Personident("12345678901"),
            kontor = kontor)

        val b = DialogmeldingBestilling(uuid = UUID.randomUUID(),
            behandler = behandler,
            arbeidstakerPersonident =  Personident("01010111111"),
            parentRef = "parent ref",
            conversationUuid =  UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
            )
        val arbeidstaker = Arbeidstaker(
            arbeidstakerPersonident = Personident("03016536325"),
            fornavn = "Ola",
            mellomnavn = "Mellomnavn",
            etternavn = "Olsen")
        val m  = DialogmeldingMapper.opprettDialogmelding(b, arbeidstaker)
       println(m.message)
    }
}

package no.nav.helse.helseidnavtest.jaxb

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.springframework.core.io.ClassPathResource
import java.util.*
import org.mockito.kotlin.whenever


//
// @ExtendWith(MockitoExtension::class)
class TestStuff {
    @Mock
    lateinit var adresse: AdresseWSAdapter
    //@Test
    fun stuff() {
        whenever(adresse.herIdForVirksomhet(any(Virksomhetsnummer::class.java))).thenReturn(12345678)
        val kontor = BehandlerKontor(
            partnerId = PartnerId(123456789),
            navn = "Et egekontor",
            orgnummer = Virksomhetsnummer(123456789),
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
            personident = Fødselsnummer("26900799232"),
            kontor = kontor)

        val b = Dialogmelding(uuid = UUID.randomUUID(),
            behandler = behandler,
            arbeidstakerPersonident =  Fødselsnummer("26900799232"),
            conversationUuid =  UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
            )
        val arbeidstaker = Arbeidstaker(
            arbeidstakerPersonident = Fødselsnummer("03016536325"),
            fornavn = "Ola",
            mellomnavn = "Mellomnavn",
            etternavn = "Olsen")
        val m  = DialogmeldingMapper(adresse).xmlFra(b, arbeidstaker)
       println(m.message)
    }
}

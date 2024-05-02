package no.nav.helse.helseidnavtest.jaxb

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
import no.nav.helseidnavtest.oppslag.person.Person.*
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
        whenever(adresse.herIdForVirksomhet(any(Virksomhetsnummer::class.java))).thenReturn(HerId(12345678))
        val kontor = BehandlerKontor("Et legekontor", "Fyrstikkalleen 1", Postnummer(1234),
             "Oslo", Virksomhetsnummer(123456789), HerId(12345678))

        val behandler =  Behandler(UUID.randomUUID(),
            Fødselsnummer("26900799232"), Navn("Ole", "Mellomnavn", "Olsen"),
            HerId(123456789), HprId(987654321), "12345678", kontor)

        val b = Dialogmelding(uuid = UUID.randomUUID(),
            behandler = behandler,
            id =  Fødselsnummer("26900799232"),
            conversationUuid =  UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
            )
        val arbeidstaker = Arbeidstaker(Fødselsnummer("03016536325"), Navn( "Ola", "Mellomnavn", "Olsen"))
        val m  = DialogmeldingMapper(adresse).xmlFra(b, arbeidstaker)
       println(m)
    }
}

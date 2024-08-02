package no.nav.helse.helseidnavtest.jaxb

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_1
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_2
import no.nav.helseidnavtest.edi20.EDI20DialogmeldingGenerator
import no.nav.helseidnavtest.edi20.EDI20DialogmeldingMapper
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import java.net.URI
import java.util.*

@ExtendWith(MockitoExtension::class)
class TestStuff {
    @Mock
    lateinit var adresse: AdresseRegisterClient

    @Mock
    lateinit var pdl: PDLClient

    //@Test
    fun stuff() {
        whenever(adresse.herIdForOrgnummer(any(Orgnummer::class.java))).thenReturn(HerId(12345678))
        val kontor = BehandlerKontor("Et legekontor", "Fyrstikkalleen 1", Postnummer(1234),
            "Oslo", Orgnummer(123456789), PartnerId(42), HerId(12345678))

        val behandler = Behandler(UUID.randomUUID(),
            Fødselsnummer("26900799232"), Navn("Ole", "Mellomnavn", "Olsen"),
            HerId(123456789), HprId(987654321), "12345678", kontor)

        val b = Dialogmelding(
            uuid = UUID.randomUUID(),
            behandler = behandler,
            id = Fødselsnummer("26900799232"),
            conversationUuid = UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
        )
        val arbeidstaker = Pasient(Fødselsnummer("03016536325"), Navn("Ola", "Mellomnavn", "Olsen"))
        val m = DialogmeldingMapper(adresse).fellesFormat(b, arbeidstaker)
        println(m)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    @Test
    fun ref() {
        whenever(pdl.navn(any(Fødselsnummer::class.java))).thenReturn(Navn("Ola", "Mellomnavn", "Olsen"))
        EDI20DialogmeldingGenerator(jaxb2Marshaller(), EDI20DialogmeldingMapper(), pdl).hodemelding(EDI_1.first,
            EDI_2.first,
            Fødselsnummer("26900799232"),
            Pair(URI.create("http://www.vg.no"), MediaType.APPLICATION_PDF_VALUE)).also { println(it) }
    }

    @Test
    fun inline() {
        whenever(pdl.navn(any(Fødselsnummer::class.java))).thenReturn(Navn("Ola", "Mellomnavn", "Olsen"))
        EDI20DialogmeldingGenerator(jaxb2Marshaller(), EDI20DialogmeldingMapper(), pdl).hodemelding(EDI_1.first,
            EDI_2.first,
            Fødselsnummer("26900799232"),
            MockMultipartFile("test", ClassPathResource("test.pdf").inputStream)).also { println(it) }
    }

    fun jaxb2Marshaller() = Jaxb2Marshaller().apply {
        setClassesToBeBound(XMLEIFellesformat::class.java,
            XMLSporinformasjonBlokkType::class.java,
            XMLMsgHead::class.java,
            XMLDialogmelding::class.java,
            XMLBase64Container::class.java,
            XMLAppRec::class.java)
    }
}

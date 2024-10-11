package no.nav.helseidnavtest.edi20

import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Fastlege
import no.nav.helseidnavtest.oppslag.person.Person
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.junit.jupiter.api.Test
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Virksomhet as FastlegeKontor

class EDI20DialogmeldingMapperTest {

    val jaxb2Marshaller = Jaxb2Marshaller().apply {
        setClassesToBeBound(
            XMLMsgHead::class.java,
            XMLDialogmelding::class.java,
            XMLBase64Container::class.java,
            XMLAppRec::class.java)
    }.createMarshaller().apply {
        setProperty(JAXB_FORMATTED_OUTPUT, true)
        setProperty(JAXB_ENCODING, "UTF-8")
    }

    val mapper = EDI20DialogmeldingMapper()

    @Test
    fun hodemelding() {
        val legekontor = FastlegeKontor(HerId("123"), "Legekontoret")
        val lege = Fastlege(HerId("456"), Person.Navn("Ola", null, "Nordmann"), legekontor)
        //val m = mapper.mottaker(Mottaker(lege, 2  Navn("Ola", null, "Nordmann")))
        //jaxb2Marshaller.marshal(m, System.out)
    }
}
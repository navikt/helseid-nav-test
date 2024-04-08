package no.nav.helseidnavtest.util

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.domain.Arbeidstaker
import no.nav.helseidnavtest.domain.DialogmeldingToBehandlerBestilling
import no.nav.helseidnavtest.util.JAXB.createFellesformat
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import java.io.StringWriter
import javax.xml.transform.stream.StreamResult

object DialogmeldingMapper {
        private val DIALOGMELDING_CONTEXT = JAXBContext.newInstance(
            XMLEIFellesformat::class.java,
            XMLMsgHead::class.java,
            XMLDialogmelding::class.java,
            XMLBase64Container::class.java,
            XMLSporinformasjonBlokkType::class.java)

    fun opprettDialogmelding(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker): Fellesformat {
        val xmleiFellesformat = createFellesformat(melding = melding, arbeidstaker = arbeidstaker)
        return Fellesformat(xmleiFellesformat, ::marshallDialogmelding)
    }

    private fun marshallDialogmelding(element: Any?): String {
        return try {
            val writer = StringWriter()
            val marshaller = DIALOGMELDING_CONTEXT.createMarshaller()
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true)
            marshaller.setProperty(JAXB_ENCODING, "UTF-8")
            marshaller.marshal(element, StreamResult(writer))
            writer.toString()
        } catch (e: JAXBException) {
            throw RuntimeException(e)
        }
    }
}
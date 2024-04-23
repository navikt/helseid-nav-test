package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.JAXB.createFellesformat
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import java.io.StringWriter
import javax.xml.transform.stream.StreamResult

object DialogmeldingMapper {
    private val MARSHALLER = JAXBContext.newInstance(
        XMLEIFellesformat::class.java,
        XMLMsgHead::class.java,
        XMLDialogmelding::class.java,
        XMLBase64Container::class.java,
        XMLSporinformasjonBlokkType::class.java).createMarshaller().apply {
            setProperty(JAXB_FORMATTED_OUTPUT, true)
            setProperty(JAXB_ENCODING, "UTF-8")
        }

    fun xmlFra(melding: Dialogmelding, arbeidstaker: Arbeidstaker) = Fellesformat(createFellesformat(melding, arbeidstaker), ::marshall)

    private fun marshall(element: Any?) =
        run {
            val writer = StringWriter()
            MARSHALLER.marshal(element, StreamResult(writer))
            "$writer"
        }
}
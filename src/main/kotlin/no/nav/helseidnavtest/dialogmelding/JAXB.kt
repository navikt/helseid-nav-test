package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLMsgHead

val MARSHALLER = JAXBContext.newInstance(
    XMLEIFellesformat::class.java,
    XMLMsgHead::class.java,
    XMLDialogmelding::class.java,
    XMLBase64Container::class.java,
    XMLSporinformasjonBlokkType::class.java).createMarshaller().apply {
    setProperty(JAXB_FORMATTED_OUTPUT, true)
    setProperty(JAXB_ENCODING, "UTF-8")
}

object ObjectFactories {
    val DMOF = no.nav.helseopplysninger.dialogmelding.ObjectFactory()
    val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
    val VOF = no.nav.helseopplysninger.basecontainer.ObjectFactory()
    val HMOF = no.nav.helseopplysninger.hodemelding.ObjectFactory()
}

















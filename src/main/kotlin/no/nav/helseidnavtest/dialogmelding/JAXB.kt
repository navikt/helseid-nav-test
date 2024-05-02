package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLCV
import no.nav.helseopplysninger.hodemelding.XMLMsgHead

val APPREC_UNMARSHALLER =
    JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java).createUnmarshaller().apply {
        // TODO setAdapter(XMLDateTimeAdapter::class.java, XMLDateTimeAdapter())
        // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())

    }
val MARSHALLER = JAXBContext.newInstance(
    XMLEIFellesformat::class.java,
    XMLMsgHead::class.java,
    XMLDialogmelding::class.java,
    XMLBase64Container::class.java,
    XMLSporinformasjonBlokkType::class.java).createMarshaller().apply {
    setProperty(JAXB_FORMATTED_OUTPUT, true)
    setProperty(JAXB_ENCODING, "UTF-8")
}

object ObjectFactories{
    val DMOF = no.nav.helseopplysninger.dialogmelding.ObjectFactory()
    val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
    val VOF = no.nav.helseopplysninger.basecontainer.ObjectFactory()
    val HMOF =  no.nav.helseopplysninger.hodemelding.ObjectFactory()
}

fun idFra(id: String, typeId: XMLCV) = HMOF.createXMLIdent().apply {
    this.id = id
    this.typeId = typeId
}

fun type(s: String, v: String, dn: String) =
    XMLCV().apply {
        this.s = s
        this.v = v
        this.dn = dn
}















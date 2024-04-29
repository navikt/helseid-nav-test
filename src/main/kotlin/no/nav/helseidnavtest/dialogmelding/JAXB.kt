package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.hodemelding.ObjectFactory

val apprecJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java)

val apprecUnmarshaller: Unmarshaller = apprecJaxBContext.createUnmarshaller().apply {

    // TODO setAdapter(XMLDateTimeAdapter::class.java, XMLDateTimeAdapter())
   // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
}

object ObjectFactories{
    val DMOF = no.nav.helseopplysninger.dialogmelding.ObjectFactory()
    val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
    val VOF = no.nav.helseopplysninger.basecontainer.ObjectFactory()
    val HMOF =  ObjectFactory()
}

















package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Innsending
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import java.io.StringWriter

@Component
class EDI20DialogmeldingGenerator(private val marshaller: Jaxb2Marshaller,
                                  private val mapper: EDI20DialogmeldingMapper) {

    fun marshal(innsending: Innsending) =
        StringWriter().run {
            marshaller.createMarshaller().marshal(mapper.hodemelding(innsending), this)
            toString()
        }

    fun unmarshal(xml: String) = marshaller.createUnmarshaller().unmarshal(xml.byteInputStream()).let {
        when (it) {
            is XMLMsgHead -> mapper.bestilling(it)
            is XMLAppRec -> mapper.apprec(it)
            else -> throw IllegalArgumentException("Unknown type: ${it.javaClass}")
        }
    }
}

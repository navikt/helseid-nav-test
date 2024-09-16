package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Innsending
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import java.io.StringWriter

@Component
class EDI20DialogmeldingGenerator(private val marshaller: Jaxb2Marshaller,
                                  private val mapper: EDI20DialogmeldingMapper) {

    fun marshal(innsending: Innsending) = marshaller.marshal(innsending)
    fun unmarshal(xml: String) = marshaller.unmarshal(xml)

    private fun Jaxb2Marshaller.marshal(innsending: Innsending) =
        StringWriter().run {
            createMarshaller().marshal(mapper.hodemelding(innsending), this)
            "$this"
        }

    private fun Jaxb2Marshaller.unmarshal(xml: String) = createUnmarshaller().unmarshal(xml.byteInputStream()).let {
        when (it) {
            // is XMLMsgHead -> mapper.innsending(it) // TODO
            is XMLAppRec -> mapper.apprec(it)
            else -> throw IllegalArgumentException("Unknown type: ${it.javaClass}")
        }
    }
}

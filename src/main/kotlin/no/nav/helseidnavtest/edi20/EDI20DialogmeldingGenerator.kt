package no.nav.helseidnavtest.edi20

import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.StringWriter
import java.net.URI

@Component
class EDI20DialogmeldingGenerator(private val marshaller: Jaxb2Marshaller,
                                  private val mapper: EDI20DialogmeldingMapper, private val pdl: PDLClient) {

    fun hodemelding(from: HerId, to: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>?) =
        StringWriter().let {
            marshaller.createMarshaller().apply {
                setProperty(JAXB_FORMATTED_OUTPUT, true)
                setProperty(JAXB_ENCODING, "UTF-8")
            }.marshal(mapper.hodemeldingVedleggReferanse(from, to, pasient(pasient), vedlegg), it)
            "$it"
        }

    fun hodemelding(from: HerId, to: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        StringWriter().let {
            marshaller.createMarshaller().apply {
                setProperty(JAXB_FORMATTED_OUTPUT, true)
                setProperty(JAXB_ENCODING, "UTF-8")
            }.marshal(mapper.hodemeldingVedleggInline(from, to, pasient(pasient), vedlegg), it)
            "$it"
        }

    private fun pasient(pasient: Fødselsnummer) = Pasient(pasient, pdl.navn(pasient))

}

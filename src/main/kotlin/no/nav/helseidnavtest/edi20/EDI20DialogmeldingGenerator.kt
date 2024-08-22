package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.StringWriter
import java.net.URI

@Component
class EDI20DialogmeldingGenerator(
    private val marshaller: Jaxb2Marshaller,
    private val adresse: AdresseRegisterClient,
    private val mapper: EDI20DialogmeldingMapper,
    private val pdl: PDLClient) {

    fun marshal(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>?) =
        StringWriter().run {
            marshaller.createMarshaller()
                .marshal(mapper.hodemelding(Bestilling(adresse.kommunikasjonsParter(fra, til), pasient(pasient)),
                    vedlegg), this)
            toString()
        }

    fun marshal(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        StringWriter().run {
            marshaller.createMarshaller()
                .marshal(mapper.hodemelding(Bestilling(adresse.kommunikasjonsParter(fra, til), pasient(pasient)),
                    vedlegg), this)
            toString()
        }

    private fun pasient(fnr: Fødselsnummer) = Pasient(fnr, pdl.navn(fnr))

    fun unmarshal(xml: String) = marshaller.createUnmarshaller().unmarshal(xml.byteInputStream()).let {
        when (it) {
            is XMLMsgHead -> mapper.bestilling(it)
            is XMLAppRec -> mapper.apprec(it)
            else -> throw IllegalArgumentException("Unknown type: ${it.javaClass}")
        }
    }
}

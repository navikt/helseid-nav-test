package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import org.slf4j.LoggerFactory.getLogger
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

    private val log = getLogger(EDI20DialogmeldingGenerator::class.java)

    fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>?) =
        StringWriter().let {
            marshaller.createMarshaller()
                .marshal(mapper.hodemelding(adresse.kommunikasjonsParter(fra, til), pasient(pasient), vedlegg),
                    it)
            "$it"
        }

    fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        StringWriter().let {
            marshaller.createMarshaller()
                .marshal(mapper.hodemelding(adresse.kommunikasjonsParter(fra, til), pasient(pasient), vedlegg),
                    it)
            "$it"
        }

    private fun pasient(fnr: Fødselsnummer) = Pasient(fnr, pdl.navn(fnr))

    fun unmarshal(apprec: String) = marshaller.createUnmarshaller().unmarshal(apprec.byteInputStream()).also {
        log.info("Unmarshalled: $it")
    }
}

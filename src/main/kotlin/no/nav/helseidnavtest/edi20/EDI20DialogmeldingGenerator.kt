package no.nav.helseidnavtest.edi20

import jakarta.xml.bind.Marshaller
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.StringWriter
import java.net.URI

@Component
class EDI20DialogmeldingGenerator(private val marshaller: Marshaller,
                                  private val mapper: EDI20DialogmeldingMapper, private val pdl: PDLClient) {

    private val log = getLogger(EDI20DialogmeldingGenerator::class.java)

    fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: Pair<URI, String>?) =
        StringWriter().let {
            marshaller.marshal(mapper.hodemelding(fra, til, pasient(pasient), vedlegg), it)
            "$it"
        }.also { log.info("Hodemelding er $it") }

    fun hodemelding(fra: HerId, til: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        StringWriter().let {
            marshaller.marshal(mapper.hodemelding(fra, til, pasient(pasient), vedlegg), it)
            "$it"
        }.also { log.info("Hodemelding er $it") }

    private fun pasient(pasient: Fødselsnummer) = Pasient(pasient, pdl.navn(pasient))

}

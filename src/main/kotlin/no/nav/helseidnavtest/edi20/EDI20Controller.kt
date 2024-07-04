package no.nav.helseidnavtest.edi20
import jakarta.xml.bind.Marshaller
import jakarta.xml.bind.Marshaller.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingGenerator
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.slf4j.LoggerFactory.getLogger
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.StringWriter
import java.util.*


@RestController(EDI20)
class EDI20Controller(private val a: EDI20RestClientAdapter, private val generator: DialogmeldingGenerator) {

    protected val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages() = a.messages()

    @GetMapping("/dialogmelding") fun dialogmelding(@RequestParam pasient: Fødselsnummer): String {
        try {
            log.trace("Genererer melding")
            val msg = generator.genererDialogmelding(pasient, UUID.randomUUID())
            log.trace("Generert melding OK")
            val writer = StringWriter()
            log.trace("Marshalling XML")
            Jaxb2Marshaller().apply {
                setMarshallerProperties(mapOf(JAXB_FORMATTED_OUTPUT to true))
                setClassesToBeBound(
                    XMLEIFellesformat::class.java,
                    XMLBase64Container::class.java,
                    XMLDialogmelding::class.java,
                    XMLMsgHead::class.java)
            }.createMarshaller().marshal(msg, writer)
            log.trace("XML {}", writer.toString())
            val b64 = Base64.getEncoder().encodeToString(writer.toString().toByteArray())
            val bd = EDI20DTOs.BusinessDocument(b64, EDI20DTOs.Properties(EDI20DTOs.System("HelseIdNavTest", "1.0.0")))
            a.postMessage(bd)
            return "OK"
        }
        catch (e: Exception) {
            log.error("Feil ved generering av dialogmelding", e)
            return "NOT OK"
        }

    }
}
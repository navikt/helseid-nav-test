package no.nav.helseidnavtest.edi20
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import no.nav.helseidnavtest.dialogmelding.DialogmeldingGenerator
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import org.slf4j.LoggerFactory.getLogger
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
        val msg =  generator.genererDialogmelding(pasient, UUID.randomUUID())
        val writer = StringWriter()
        val context: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java)
        val m: Marshaller = context.createMarshaller()
        m.marshal(msg, writer)
        log.info("Dialogmelding for pasient $pasient: $writer")
        return "OK"
    }




}
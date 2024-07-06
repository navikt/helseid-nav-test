package no.nav.helseidnavtest.edi20
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.websocket.server.PathParam
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController(EDI20)
class EDI20Controller(private val a: EDI20Service) {

    private val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages(@Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @RequestParam herId: String) = a.poll(HerId(herId))

    @GetMapping("/dialogmelding") fun dialogmelding(@Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @RequestParam herId: String): String {
        runCatching {
            a.send(HerId(herId))
            return "OK"
        }.getOrElse {
            log.error("Feil ved generering av dialogmelding", it)
            return "NOT OK"
        }
    }
    @PutMapping("/messages/{uuid}/read/{herId}")
    fun markRead(@PathVariable uuid: UUID, @Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @PathVariable herId: String) =
        a.markRead(uuid, HerId(herId))
}

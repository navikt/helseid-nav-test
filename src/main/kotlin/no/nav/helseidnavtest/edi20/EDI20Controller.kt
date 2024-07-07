package no.nav.helseidnavtest.edi20
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController(EDI20)
@RequestMapping("/$EDI20")
class EDI20Controller(private val a: EDI20Service) {

    @GetMapping("/messages") fun messages(@Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @RequestParam herId: String,
                                          @RequestParam(defaultValue = "false") apprec: Boolean) =
        a.poll(HerId(herId))

    @GetMapping("/dialogmelding") fun dialogmelding(@Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @RequestParam herId: String) =
        a.send(HerId(herId))

    @GetMapping("/mark/{uuid}/read/{herId}")
    fun markRead(@PathVariable uuid: UUID,
                 @Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) @PathVariable herId: String) =
        a.markRead(uuid, HerId(herId))

    @GetMapping("/messages/{uuid}/", produces = [APPLICATION_XML_VALUE])
    fun hentDokument(@PathVariable uuid: UUID,
                     @Parameter(schema = Schema(allowableValues = arrayOf(EDI1_ID, EDI2_ID))) herId: String) =
        a.hent(uuid, HerId(herId))
}
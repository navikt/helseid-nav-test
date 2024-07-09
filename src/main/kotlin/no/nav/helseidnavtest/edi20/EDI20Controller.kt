package no.nav.helseidnavtest.edi20
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.DOK_PATH
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MESSAGES_PATH
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController(EDI20)
@RequestMapping("/$EDI20/")
class EDI20Controller(private val service: EDI20Service) {

    @PostMapping("${DOK_PATH}Apprec") fun apprec(@PathVariable id: UUID, @Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) @RequestParam herId: String) =
        service.apprec(id,HerId(herId))

    @GetMapping(MESSAGES_PATH) fun poll(@Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) @RequestParam herId: String, @RequestParam(defaultValue = "false") apprec: Boolean) =
        service.poll(HerId(herId),apprec)

    @PostMapping(MESSAGES_PATH) fun send(@Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) @RequestParam herId: String) =
        service.send(HerId(herId))

    @PutMapping("${DOK_PATH}read/{herId}")
    fun lest(@PathVariable id: UUID, @Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) @PathVariable herId: String) =
        service.lest(id, HerId(herId))

    @GetMapping(DOK_PATH, produces = [APPLICATION_XML_VALUE])
    fun les(@PathVariable id: UUID, @Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) herId: String) =
        service.les(id, HerId(herId))

    @GetMapping("${DOK_PATH}status")
    fun status(@PathVariable id: UUID,@Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID])) herId: String) =
        service.status(id, HerId(herId))
}
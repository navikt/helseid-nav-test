package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.DOK_PATH
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MESSAGES_PATH
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController(EDI20)
@RequestMapping("/$EDI20/")
class EDI20Controller(private val edi: EDI20Service) {

    @PostMapping("${DOK_PATH}/apprec")
    fun apprec(@Herid @RequestParam herId: HerId, @PathVariable id: UUID) = edi.apprec(herId, id)

    @GetMapping(MESSAGES_PATH)
    fun poll(@Herid @RequestParam herId: HerId, @RequestParam(defaultValue = "false") apprec: Boolean) =
        edi.poll(herId, apprec)

    @PostMapping(MESSAGES_PATH, consumes = [MULTIPART_FORM_DATA_VALUE])
    fun send(@RequestPart("file") vedlegg: MultipartFile?, @Herid @RequestParam herId: HerId) = edi.send(herId, vedlegg)

    @PutMapping("${DOK_PATH}/read/{herId}")
    fun lest(@Herid @PathVariable herId: HerId, @PathVariable id: UUID) = edi.lest(herId, id)

    @GetMapping(DOK_PATH, produces = [APPLICATION_XML_VALUE])
    fun les(@Herid herId: HerId, @PathVariable id: UUID) = edi.les(herId, id)

    @GetMapping("${DOK_PATH}/status")
    fun status(@Herid herId: HerId, @PathVariable id: UUID) = edi.status(herId, id)
}
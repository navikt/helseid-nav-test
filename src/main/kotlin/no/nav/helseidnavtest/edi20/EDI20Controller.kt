package no.nav.helseidnavtest.edi20


import no.nav.helseidnavtest.dialogmelding.HerId.Companion.of
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.DOK_PATH
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MESSAGES_PATH
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController(EDI20)
@RequestMapping("/$EDI20/")
class EDI20Controller(private val edi: EDI20Service) {


    @PostMapping("${DOK_PATH}/Apprec")
    fun apprec(@Herid @RequestParam herId: String,
               @PathVariable id: UUID) =
        edi.apprec(of(herId), id)

    @GetMapping(MESSAGES_PATH)
    fun poll(@Herid @RequestParam herId: String,
             @RequestParam(defaultValue = "false") apprec: Boolean) =
        edi.poll(of(herId), apprec)

    @PostMapping(MESSAGES_PATH)
    fun send(@Herid @RequestParam herId: String) =
        edi.send(of(herId))

    @PutMapping("${DOK_PATH}/read/{herId}")
    fun lest(@Herid @PathVariable herId: String,
             @PathVariable id: UUID) =
        edi.lest(of(herId), id)

    @GetMapping(DOK_PATH, produces = [APPLICATION_XML_VALUE])
    fun les(@Herid herId: String,
            @PathVariable id: UUID) =
        edi.les(of(herId), id)

    @GetMapping("${DOK_PATH}/status")
    fun status(@Herid herId: String,
               @PathVariable id: UUID) =
        edi.status(of(herId), id)
}
package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
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

    @Operation(description = "Sender apprec for melding for gitt avsender")
    @PostMapping("${DOK_PATH}/apprec")
    fun apprec(@Herid @RequestParam herId: HerId, @PathVariable id: UUID) = edi.apprec(herId, id)

    @Operation(description = "Henter uleste meldinger for gitt herId")
    @GetMapping(MESSAGES_PATH)
    fun poll(@Herid @RequestParam herId: HerId,
             @Parameter(description = "Inkluderer apprec hvis satt") @RequestParam(defaultValue = "false") apprec: Boolean) =
        edi.poll(herId, apprec)

    @Operation(description = "Laster opp et vedlegg og inkluderer denne som en Deft-referanse i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/ref", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendRef(@RequestPart("file", required = false) vedlegg: MultipartFile?,
                @RequestParam(defaultValue = "26900799232") pasient: String,
                @Herid @RequestParam herId: HerId) =
        edi.sendRef(herId, Fødselsnummer(pasient), vedlegg)

    @PostMapping("$MESSAGES_PATH/ref/show", consumes = [MULTIPART_FORM_DATA_VALUE])

    @Operation(description = "Laster opp et vedlegg og viser hodemeldingen slik den ville ha blitt sendt som en Deft-referanse for den gitte avsenderen")
    fun showRef(@RequestPart("file", required = false) vedlegg: MultipartFile,
                @RequestParam(defaultValue = "26900799232") pasient: String,
                @Herid @RequestParam herId: HerId) =
        edi.showRef(herId, Fødselsnummer(pasient), vedlegg)

    @Operation(description = "Laster opp et vedlegg og inkluderer denne inline i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendInline(@RequestPart("file", required = false) vedlegg: MultipartFile?,
                   @RequestParam(defaultValue = "26900799232") pasient: String,
                   @Herid @RequestParam herId: HerId) =
        edi.sendInline(herId, Fødselsnummer(pasient), vedlegg)

    @Operation(description = "Laster opp et vedlegg og viser hodemeldingen slik den ville ha blitt sendt inline for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline/show", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun showInline(@RequestPart("file", required = false) vedlegg: MultipartFile,
                   @RequestParam(defaultValue = "26900799232") pasient: String,
                   @Herid @RequestParam herId: HerId) =
        edi.showInline(herId, Fødselsnummer(pasient), vedlegg)

    @Operation(description = "Marker et dokument som konsumert av en gitt herId")
    @PutMapping("${DOK_PATH}/read/{herId}")
    fun konsumert(@Herid @PathVariable herId: HerId, @PathVariable id: UUID) = edi.konsumert(herId, id)

    @Operation(description = "Les et dokument som en gitt herId")
    @GetMapping(DOK_PATH, produces = [APPLICATION_XML_VALUE])
    fun les(@Herid herId: HerId, @PathVariable id: UUID) = edi.les(herId, id)

    @Operation(description = "Les status for et dokument som en gitt herId")
    @GetMapping("${DOK_PATH}/status")
    fun status(@Herid herId: HerId, @PathVariable id: UUID) = edi.status(herId, id)
}
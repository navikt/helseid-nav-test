package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.DOK_PATH
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_1
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_2
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.MESSAGES_PATH
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.VALIDATOR
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import no.nav.helseidnavtest.oppslag.person.PDLClient
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController(EDI20)
@RequestMapping("/$EDI20/")
@Tag(name = "EDI2.0",
    description = "Controller for å teste EDI2.0-apiet, kaller videre til NHN-apiet med auth (Client Credential Flow) for valgt herId",
    externalDocs = ExternalDocumentation(description = "EDI 2.0",
        url = "https://utviklerportal.nhn.no/informasjonstjenester/meldingstjener/edi-20/edi-20-ekstern-docs/openapi/meldingstjener-api/"))
class EDI20Controller(
    private val edi: EDI20Service,
    private val deft: EDI20DeftService,
    private val pdl: PDLClient,
    private val adresse: AdresseRegisterClient,
    private val generator: EDI20DialogmeldingGenerator
) {

    @Operation(description = "Sender apprec for melding for gitt mottaker")
    @PostMapping("${DOK_PATH}/apprec")
    fun apprec(@Herid
               @RequestParam herId: HerId,
               @Parameter(description = "Dokument-id")
               @PathVariable id: UUID) =
        edi.apprec(herId, id)

    @Operation(description = "Henter uleste meldinger for en gitt herId")
    @GetMapping(MESSAGES_PATH)
    fun poll(@Herid
             @RequestParam herId: HerId,
             @Parameter(description = "Spesifiserer om apprec-meldinger skal inkluderes eller ikke")
             @RequestParam(defaultValue = "false") apprec: Boolean) =
        edi.poll(herId, apprec)

    @Operation(description = "Laster opp et vedlegg og inkluderer denne som en Deft-referanse i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/ref", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendRef(@Herid
                @RequestParam fra: HerId,
                @Parameter(description = "Pasientens fødselsnummer")
                @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
                @Parameter(description = "Valgfritt vedlegg")
                @RequestPart("file", required = false) vedlegg: MultipartFile?) =
        vedlegg?.let {
            edi.send(ref(fra, pasient = pasient, vedlegg = it))
        } ?: edi.send(Bestilling(adresse.kommunikasjonsParter(fra),
            Pasient(Fødselsnummer(pasient), pdl.navn(Fødselsnummer(pasient)))))

    @PostMapping("$MESSAGES_PATH/ref/show", consumes = [MULTIPART_FORM_DATA_VALUE])

    @Operation(description = "Laster opp et vedlegg og viser hodemeldingen slik den ville ha blitt sendt som en Deft-referanse for den gitte avsenderen")
    fun showRef(@Herid
                @RequestParam fra: HerId,
                @Parameter(description = "Pasientens fødselsnummer")
                @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
                @Parameter(description = "Vedlegg")
                @RequestPart("file", required = false) vedlegg: MultipartFile) =
        generator.marshal(ref(fra, pasient = pasient, vedlegg = vedlegg))

    @Operation(description = "Laster opp et vedlegg og inkluderer denne inline i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendInline(@Herid
                   @RequestParam fra: HerId,
                   @Parameter(description = "Pasientens fødselsnummer")
                   @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
                   @Parameter(description = "Valgfritt vedlegg")
                   @RequestPart("file", required = false) vedlegg: MultipartFile?) =
        edi.send(inline(fra, pasient = pasient, vedlegg = vedlegg))

    @Operation(description = "Laster opp et vedlegg og inkluderer denne inline i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inlinevalidering", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendInlineTilValidering(@Herid
                                @RequestParam fra: HerId,
                                @RequestParam(defaultValue = VALIDATOR) til: HerId,
                                @Parameter(description = "Pasientens fødselsnummer")
                                @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
                                @Parameter(description = "Valgfritt vedlegg")
                                @RequestPart("file", required = false) vedlegg: MultipartFile?) =
        edi.send(inline(fra, til, pasient, vedlegg))

    @Operation(description = "Laster opp et vedlegg og viser hodemeldingen slik den ville ha blitt sendt inline for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline/show", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun showInline(@Herid
                   @RequestParam fra: HerId,
                   @Parameter(description = "Pasientens fødselsnummer")
                   @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
                   @Parameter(description = "Vedlegg")
                   @RequestPart("file", required = false) vedlegg: MultipartFile) =
        generator.marshal(inline(fra, pasient = pasient, vedlegg = vedlegg))

    @Operation(description = "Marker et dokument som konsumert av en gitt herId")
    @PutMapping("${DOK_PATH}/read/{herId}")
    fun konsumert(@Herid
                  @PathVariable herId: HerId,
                  @Parameter(description = "Dokument-id")
                  @PathVariable id: UUID) =
        edi.konsumert(herId, id)

    @Operation(description = "Les et dokument for en gitt herId")
    @GetMapping(DOK_PATH)
    fun les(@Herid herId: HerId,
            @Parameter(description = "Dokument-id")
            @PathVariable id: UUID) =
        edi.les(herId, id)

    @Operation(description = "Les status for et dokument for en gitt herId")
    @GetMapping("${DOK_PATH}/status")
    fun status(@Herid herId: HerId,
               @Parameter(description = "Dokument-id")
               @PathVariable id: UUID) =
        edi.status(herId, id)

    @Operation(description = "Merk alle dokumenter lest for  $EDI1_ID og $EDI2_ID")
    @GetMapping("${MESSAGES_PATH}/lesalle")
    fun lesOgAckAlle() = lesOgAck(EDI_1.first) + lesOgAck(EDI_2.first)

    private fun lesOgAck(herId: HerId) =
        edi.poll(herId, true)
            ?.flatMap { m ->
                m.messageIds.map {
                    konsumert(m.herId, it)
                    //  apprec(m.herId, it)
                    it
                }
            } ?: emptyList()

    private fun inline(fra: HerId, til: HerId = fra.other(), pasient: String, vedlegg: MultipartFile?) =
        Bestilling(adresse.kommunikasjonsParter(fra, til),
            Pasient(Fødselsnummer(pasient), pdl.navn(Fødselsnummer(pasient))),
            vedlegg)

    private fun ref(fra: HerId, til: HerId = fra.other(), pasient: String, vedlegg: MultipartFile) =
        Bestilling(adresse.kommunikasjonsParter(fra, til),
            Pasient(Fødselsnummer(pasient), pdl.navn(Fødselsnummer(pasient))),
            ref = Pair(deft.upload(fra, vedlegg), vedlegg.contentType!!))
}
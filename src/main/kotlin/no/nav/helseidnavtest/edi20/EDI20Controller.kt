package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
import no.nav.helseidnavtest.edi20.EDI20Config.PollParameters
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.adresse.Innsending
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.User
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.UUID.randomUUID
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer as Fnr

private const val NO_TOKEN = "Mangler OIDC-token"

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
    private val generator: EDI20DialogmeldingMarshaller
) {

    private val log = getLogger(javaClass)

    @Operation(description = "Sender apprec for melding for gitt mottaker")
    @PostMapping("${DOK_PATH}/apprec", produces = [APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE])
    fun apprec(
        @Herid @RequestParam herId: HerId,
        @Parameter(description = "Dokument-id")
        @PathVariable id: UUID) =
        edi.apprec(herId, id)

    @Operation(description = "Henter uleste meldinger for en gitt herId")
    @GetMapping(MESSAGES_PATH)
    fun poll(
        @Herid @RequestParam herId: HerId,
        @Parameter(description = "Spesifiserer om apprec-meldinger skal inkluderes eller ikke")
        @RequestParam(defaultValue = "false") apprec: Boolean) =
        edi.poll(PollParameters(herId, apprec))

    @Operation(description = "Laster opp et vedlegg og inkluderer denne inline i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun send(
        @AuthenticationPrincipal helsePersonell: OidcUser?,
        @Herid @RequestParam fra: HerId,
        @Parameter(description = "Pasientens fødselsnummer")
        @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
        @Parameter(description = "Valgfritt vedlegg")
        @RequestPart("file", required = false) vedlegg: MultipartFile?) =
        helsePersonell?.let {
            edi.send(bestilling(fra, fra.other(), pasient, ClaimsExtractor(it).user, vedlegg))
        } ?: throw IrrecoverableException(UNAUTHORIZED, edi.uri, NO_TOKEN)

    @Operation(description = "Laster opp et vedlegg og inkluderer denne inline i hodemeldingen for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inlinevalidering", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun valider(
        @AuthenticationPrincipal helsePersonell: OidcUser?,
        @Herid @RequestParam fra: HerId,
        @RequestParam(defaultValue = VALIDATOR) til: HerId,
        @Parameter(description = "Pasientens fødselsnummer")
        @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
        @Parameter(description = "Valgfritt vedlegg")
        @RequestPart("file", required = false) vedlegg: MultipartFile?) =
        helsePersonell?.let {
            edi.send(bestilling(fra, til, pasient, ClaimsExtractor(it).user, vedlegg))
        } ?: throw IrrecoverableException(UNAUTHORIZED, edi.uri, NO_TOKEN)

    @Operation(description = "Laster opp et vedlegg og viser hodemeldingen slik den ville ha blitt sendt inline for den gitte avsenderen")
    @PostMapping("$MESSAGES_PATH/inline/show", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun show(
        @AuthenticationPrincipal helsePersonell: OidcUser?,
        @Herid @RequestParam fra: HerId,
        @Parameter(description = "Pasientens fødselsnummer")
        @RequestParam(defaultValue = DEFAULT_PASIENT) pasient: String,
        @Parameter(description = "Vedlegg")
        @RequestPart("file", required = false) vedlegg: MultipartFile) =
        helsePersonell?.let {
            generator.marshal(bestilling(fra, fra.other(), pasient, ClaimsExtractor(it).user, vedlegg))
        } ?: throw IrrecoverableException(UNAUTHORIZED, edi.uri, NO_TOKEN)

    @Operation(description = "Marker et dokument som konsumert av en gitt herId")
    @PutMapping("${DOK_PATH}/read/{herId}")
    fun konsumert(
        @Herid @PathVariable herId: HerId,
        @Parameter(description = "Dokument-id")
        @PathVariable id: UUID) =
        edi.konsumert(herId, id)

    @Operation(description = "Les et dokument for en gitt herId")
    @GetMapping("$DOK_PATH/business-dokument", produces = [APPLICATION_XML_VALUE])
    fun les(
        @Herid herId: HerId,
        @Parameter(description = "Dokument-id")
        @PathVariable id: UUID) =
        edi.raw(herId, id)

    @Operation(description = "Les status for et dokument for en gitt herId")
    @GetMapping("${DOK_PATH}/status")
    fun status(
        @Herid herId: HerId, @Parameter(description = "Dokument-id")
        @PathVariable id: UUID) =
        edi.status(herId, id)

    @Operation(description = "Merk alle dokumenter lest for  $EDI1_ID og $EDI2_ID")
    @GetMapping("${MESSAGES_PATH}/lesalle")
    fun lesOgAckAlle() = mapOf(EDI_1 to lesOgAck(EDI_1.first), EDI_2 to lesOgAck(EDI_2.first))

    private fun lesOgAck(herId: HerId) =
        edi.poll(PollParameters(herId))
            .flatMap { m ->
                m.receiverHerId.map {
                    m.senderHerId?.let { s -> edi.konsumert(s, m.id) }
                    it
                }
            }

    private fun bestilling(fra: HerId, til: HerId, pasient: String, user: User, vedlegg: MultipartFile?) =
        Innsending(randomUUID(),
            adresse.parter(fra, til, user.navn),
            Pasient(Fnr(pasient), pdl.navn(Fnr(pasient))),
            vedlegg?.bytes)

}
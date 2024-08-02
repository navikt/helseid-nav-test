package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.OBJECT_PATH
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController(EDI20DEFT)
@RequestMapping("/$EDI20DEFT/")
@Tag(name = "EDI2.0",
    description = "Controller for å teste EDI2.0-Deft apiet, kaller videre til NHN-apiet med auth (Client Credential Flow) for valgt herId",
    externalDocs = ExternalDocumentation(description = "EDI 2.0",
        url = "https://utviklerportal.nhn.no/informasjonstjenester/meldingstjener/edi-20/edi-20-ekstern-docs/openapi/deft-api-store-filer/"))
class EDI20DeftController(private val deft: EDI20DeftService) {

    @Operation(description = "Laster opp fil for gitt mottaker")
    @PostMapping(OBJECT_PATH, consumes = [MULTIPART_FORM_DATA_VALUE])
    fun upload(@Herid
               @RequestParam herId: HerId,
               @RequestPart("file")
               @Parameter(description = "Vedlegg")
               file: MultipartFile) =
        deft.upload(herId, file)

    @Operation(description = "Henter status for fil med gitt nøkkel")
    @GetMapping("$OBJECT_PATH/{key}/status")
    fun status(@Herid
               @RequestParam herId: HerId,
               @Parameter(description = "Nøkkel for fil")
               key: String) =
        deft.status(herId, key)

    @Operation(description = "Henter fil med gitt URL")
    @GetMapping(OBJECT_PATH)
    fun les(@Herid
            @RequestParam herId: HerId,
            @Parameter(description = "Dokument-URL")
            uri: URI) =
        deft.les(herId, uri)

    @Operation(description = "Sletter fil med gitt URL")
    @DeleteMapping(OBJECT_PATH)
    fun slett(@Herid @RequestParam herId: HerId,
              @Parameter(description = "Dokument-URL")
              uri: URI) =
        deft.slett(herId, uri)

    @Operation(description = "Sletter fil med gitt nøkkel")
    @DeleteMapping("$OBJECT_PATH/{key}")
    fun slett(@Herid
              @RequestParam herId: HerId,
              @Parameter(description = "Nøkkel for fil")
              @PathVariable key: String) =
        deft.slett(herId, key)

    @Operation(description = "Kvitterer fil med gitt nøkkel")
    @PutMapping("$OBJECT_PATH/{key}")
    fun kvitter(@Herid
                @RequestParam herId: HerId,
                @Parameter(description = "Nøkkel for fil")
                @PathVariable key: String) =
        deft.kvitter(herId, key)
}

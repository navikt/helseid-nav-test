package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.OBJECT_PATH
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController(EDI20DEFT)
@RequestMapping("/$EDI20DEFT/")
class EDI20DeftController(private val deft: EDI20DeftService) {

    @PostMapping(OBJECT_PATH, consumes = [MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart("file") file: MultipartFile, @Herid @RequestParam herId: HerId) =
        deft.upload(file, herId)

    @GetMapping("$OBJECT_PATH/{key}/status")
    fun status(key: String, @Herid @RequestParam herId: HerId) =
        deft.status(key, herId)

    @GetMapping(OBJECT_PATH)
    fun les(uri: URI, @Herid @RequestParam herId: HerId) =
        deft.les(uri, herId)

    @DeleteMapping(OBJECT_PATH)
    fun slett(uri: URI, @Herid @RequestParam herId: HerId) =
        deft.slett(uri, herId)

    @DeleteMapping("$OBJECT_PATH/{key}")
    fun slett(@PathVariable key: String, @Herid @RequestParam herId: HerId) =
        deft.slett(key, herId)

    @PutMapping("$OBJECT_PATH/{key}")
    fun kvitter(@PathVariable key: String, @Herid @RequestParam herId: HerId) =
        deft.kvitter(key, herId)
}

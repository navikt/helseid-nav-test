package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId.Companion.of
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
    fun upload(@RequestPart("file") file: MultipartFile, @Herid @RequestParam herId: String) =
        deft.upload(file, of(herId))

    @GetMapping(OBJECT_PATH)
    fun les(uri: URI, @Herid @RequestParam herId: String) =
        deft.les(uri, of(herId))

    @DeleteMapping(OBJECT_PATH)
    fun slett(uri: URI, @Herid @RequestParam herId: String) =
        deft.slett(uri, of(herId))

    @DeleteMapping(OBJECT_PATH)
    fun slett(key: String, @Herid @RequestParam herId: String) =
        deft.slett(key, of(herId))
}

package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId.Companion.of
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.OBJECT_PATH
import org.springframework.http.MediaType.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController(EDI20DEFT)
@RequestMapping("/$EDI20DEFT/")
class EDI20DeftController(private val deft: EDI20DeftService) {

    @PostMapping(OBJECT_PATH, consumes = [MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("file") file: MultipartFile,@Herid @RequestParam herId: String) =
       deft.upload(file.inputStream, of(herId)).also { println("Uploaded $file")}
}

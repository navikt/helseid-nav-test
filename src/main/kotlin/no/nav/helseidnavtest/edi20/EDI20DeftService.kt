package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID

@Service
class EDI20DeftService(val adapter: EDI20DeftRestClientAdapter) {
   fun upload(file: MultipartFile, id: HerId) = adapter.upload(file,id.verdi)
}
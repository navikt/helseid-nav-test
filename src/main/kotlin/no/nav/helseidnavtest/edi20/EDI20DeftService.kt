package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EDI20DeftService(val adapter: EDI20DeftRestClientAdapter) {
   fun upload(bytes: ByteArray, id: HerId) = adapter.upload(bytes,id.verdi)
}
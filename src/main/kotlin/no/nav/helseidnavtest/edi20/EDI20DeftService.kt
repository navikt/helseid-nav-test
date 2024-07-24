package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Service
class EDI20DeftService(val adapter: EDI20DeftRestClientAdapter) {
    fun upload(file: MultipartFile, id: HerId) = adapter.upload(file, id.verdi)
    fun les(uri: URI, id: HerId) = adapter.les(uri, id.verdi)
    fun kvitter(key: String, id: HerId) = adapter.kvitter(key, id.verdi)
    fun status(key: String, id: HerId) = adapter.status(key, id.verdi)
    fun slett(uri: URI, id: HerId) = slett(uri.key(), id)
    fun slett(key: String, id: HerId) = adapter.slett(key, id.verdi)
}
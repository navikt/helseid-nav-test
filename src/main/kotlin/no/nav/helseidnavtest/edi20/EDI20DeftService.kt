package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Service
class EDI20DeftService(val adapter: EDI20DeftRestClientAdapter) {
    fun upload(id: HerId, file: MultipartFile) = adapter.upload(id, file)
    fun les(id: HerId, uri: URI) = adapter.les(id, uri)
    fun kvitter(id: HerId, key: String) = adapter.kvitter(id, key)
    fun status(id: HerId, key: String) = adapter.status(id, key)
    fun slett(id: HerId, uri: URI) = slett(id, uri.key())
    fun slett(id: HerId, key: String) = adapter.slett(id, key)
}
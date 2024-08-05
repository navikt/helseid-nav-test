package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(EDI20DEFT)
class EDI20DeftConfig(baseUri: URI, pingPath: String = DEFAULT_PING_PATH, enabled: Boolean = true) :
    AbstractRestConfig(baseUri, pingPath, EDI20DEFT, enabled) {

    fun uploadURI(b: UriBuilder, herId: HerId) =
        b.path(OBJECT_PATH)
            .queryParam(SENDER_HER_ID, herId.verdi)
            .queryParam(RECEIVER_HER_IDS, herId.other().verdi)
            .build()

    fun deleteURI(b: UriBuilder, key: String) = b.path(KEY_PATH).build(key)

    fun statusURI(b: UriBuilder, key: String) = b.path(STATUS_PATH).build(key)

    fun kvitteringURI(b: UriBuilder, key: String, herId: HerId) = b.path(KVITTERING_PATH).build(key, herId.verdi)

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val EDI20DEFT = "edi20deft"
        const val OBJECT_PATH = "objects"
        private const val DEFAULT_PING_PATH = ""
        private const val SENDER_HER_ID = "SenderHerId"
        private const val RECEIVER_HER_IDS = "ReceiverHerIds"
        private const val KEY_PATH = "$OBJECT_PATH/{key}"
        private const val STATUS_PATH = "$KEY_PATH/DownloadStatus"
        private const val KVITTERING_PATH = "$KEY_PATH/DownloadReceipt/{herId}"
    }
}
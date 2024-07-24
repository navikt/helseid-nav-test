package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(EDI20DEFT)
class EDI20DeftConfig(baseUri: URI, pingPath: String = DEFAULT_PING_PATH, enabled: Boolean = true) :
    AbstractRestConfig(baseUri, pingPath, EDI20DEFT, enabled) {

    fun uploadURI(b: UriBuilder, herId: String) =
        b.path(OBJECT_PATH)
            .queryParam(SENDER_HER_ID, herId)
            .queryParam(RECEIVER_HER_IDS, herId.other())
            .build()

    fun deleteURI(b: UriBuilder, key: String) =
        b.path(KEY_PATH).build(key)

    fun statusURI(b: UriBuilder, key: String) =
        b.path(STATUS_PATH).build(key)

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        private const val DEFAULT_PING_PATH = ""
        private const val SENDER_HER_ID = "SenderHerId"
        private const val RECEIVER_HER_IDS = "ReceiverHerIds"
        const val EDI20DEFT = "edi20deft"
        const val OBJECT_PATH = "objects"
        const val KEY_PATH = "$OBJECT_PATH/{key}"
        const val STATUS_PATH = "$KEY_PATH/DownloadStatus"

    }
}
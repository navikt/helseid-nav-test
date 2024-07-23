package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.edi20.EDI20Utils.other
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.util.UriBuilder



@ConfigurationProperties(EDI20DEFT)
class EDI20DeftConfig(baseUri: URI, pingPath: String = DEFAULT_PING_PATH, enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, EDI20, enabled) {
    protected val log = getLogger(EDI20DeftConfig::class.java)


    fun uploadURI(b: UriBuilder, herId: String) =
        b.path(OBJECT_PATH)
            .queryParam(SENDER_HER_ID, herId)
            .queryParam(RECEIVER_HER_IDS, herId.other())
            .build().also { log.info("URI er $it") }

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        private const val DEFAULT_PING_PATH = ""
        private const val SENDER_HER_ID = "SenderHerId"
        private const val RECEIVER_HER_IDS = "ReceiverHerIds"
        const val EDI20DEFT = "edi20deft"
        const val OBJECT_PATH = "objects"
    }
}
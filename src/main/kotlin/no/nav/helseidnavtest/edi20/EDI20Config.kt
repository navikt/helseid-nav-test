package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.util.UriBuilder
import java.lang.Boolean.FALSE

@ConfigurationProperties(EDI20)
class EDI20Config(baseUri: URI,
                  pingPath: String = DEFAULT_PING_PATH,
                   private val messagesPath : String = DEFAULT_MESSAGES_PATH,
                  enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, EDI20, enabled) {

    private val log = getLogger(EDI20Config::class.java)

    fun messagesPostURI(b: UriBuilder) = b.path(messagesPath).build().also { log.info("messagesPostURI: $it")}

    fun messagesURI(b: UriBuilder, herId: HerId) = b
        .path(messagesPath)
        .queryParam("IncludeAppRec", FALSE)
        .queryParam("ToHerIds",herId.verdi)
        .build().also { log.info("messagesURI: $it")}

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val SENDER_ID = "8142519"
        const val MOTTAKER_ID = "8142520"
        val SENDER  = HerId(SENDER_ID) to "edi20-1"
        val MOTTAGER = HerId(MOTTAKER_ID) to "edi20-2"
        const val DEFAULT_MESSAGES_PATH = "/messages"
        const val EDI20 = "edi20"
        const val HERID = "herId"
        private const val DEFAULT_PING_PATH = ""
    }
}
package no.nav.helseidnavtest.edi20

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

    fun messagesURI(b: UriBuilder) = b
        .path(messagesPath)
        .queryParam("IncludeAppRec", FALSE)
        .queryParam("ToHerIds",8142519)
        .build()

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val DEFAULT_MESSAGES_PATH = "/messages"
        const val EDI20 = "edi20"
        private const val DEFAULT_PING_PATH = ""
    }
}
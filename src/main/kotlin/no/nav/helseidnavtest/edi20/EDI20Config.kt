package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.util.UriBuilder
import java.lang.Boolean.FALSE
import java.util.*

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

    fun messagesURI(b: UriBuilder, uuid: UUID) =
        b.path(DOK_PATH).build(uuid.toString())


    fun markReadURI(b: UriBuilder, id: UUID, herId: HerId) = b
        .path("/messages/$id/read/${herId.verdi}")
        .build().also { log.info("markReadUrl: $it")}

    fun statusURI(b: UriBuilder, id: UUID) =
        b.path("/messages/$id/status")
            .build(id.toString())

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"


    companion object {
        const val EDI1_ID = "8142519"
        const val EDI2_ID = "8142520"
        val EDI_1  = HerId(EDI1_ID) to "edi20-1"
        val EDI_2 = HerId(EDI2_ID) to "edi20-2"
        const val DEFAULT_MESSAGES_PATH = "/messages"
        const val DOK_PATH  = DEFAULT_MESSAGES_PATH + "/{uuid}"
        const val EDI20 = "edi20"
        const val HERID = "herId"
        private const val DEFAULT_PING_PATH = ""
    }
}
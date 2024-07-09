package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.util.UriBuilder
import java.util.*

@ConfigurationProperties(EDI20)
class EDI20Config(baseUri: URI,
                  pingPath: String = DEFAULT_PING_PATH,
                  enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, EDI20, enabled) {

    private val log = getLogger(EDI20Config::class.java)

    fun messagesPostURI(b: UriBuilder) =
        b.path(MESSAGES_PATH).build().also { log.info("messagesPostURI: $it")}

    fun messagesURI(b: UriBuilder, herId: HerId, appRec: Boolean) = b
        .path(MESSAGES_PATH)
        .queryParam("IncludeAppRec", "$appRec")
        .queryParam("ToHerIds",herId.verdi)
        .build()

    fun messagesURI(b: UriBuilder, id: UUID) =
        b.path(DOK_PATH).build("$id")

    fun markReadURI(b: UriBuilder, id: UUID, herId: HerId) = b
        .path("$DOK_PATH/read/${herId.verdi}")
        .build("$id")

    fun statusURI(b: UriBuilder, id: UUID) =
        b.path("$DOK_PATH/status")
            .build("$id")

    fun kvitteringURI(b: UriBuilder, id: UUID, other: String) =
        b.path("$DOK_PATH/Apprec/$other")
            .build("$id")


    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"


    companion object {
        const val EDI1_ID = "8142519"
        const val EDI2_ID = "8142520"
        val EDI_1  = HerId(EDI1_ID) to "edi20-1"
        val EDI_2 = HerId(EDI2_ID) to "edi20-2"
        const val MESSAGES_PATH = "/messages"
        const val DOK_PATH  = "$MESSAGES_PATH/{id}"
        const val EDI20 = "edi20"
        const val HERID = "herId"
        private const val DEFAULT_PING_PATH = ""
    }
}
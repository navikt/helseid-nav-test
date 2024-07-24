package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.util.*

@ConfigurationProperties(EDI20)
class EDI20Config(baseUri: URI, pingPath: String = DEFAULT_PING_PATH, enabled: Boolean = true) :
    AbstractRestConfig(baseUri, pingPath, EDI20, enabled) {

    fun sendURI(b: UriBuilder) =
        b.path(MESSAGES_PATH).build()

    fun pollURI(b: UriBuilder, herId: String, appRec: Boolean) =
        b.path(MESSAGES_PATH)
            .queryParam("IncludeAppRec", "$appRec")
            .queryParam("ToHerIds", herId)
            .build()

    fun lesURI(b: UriBuilder, id: UUID) = b.path(DOK_PATH).build("$id")

    fun lestURI(b: UriBuilder, id: UUID, herId: String) =
        b.path("$DOK_PATH/read/$herId").build("$id")

    fun statusURI(b: UriBuilder, id: UUID) =
        b.path("$DOK_PATH/status").build("$id")

    fun apprecURI(b: UriBuilder, id: UUID, other: String) =
        b.path("$DOK_PATH/Apprec/$other").build("$id")

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val EDI20 = "edi20"
        const val EDI1_ID = "8142519"
        const val EDI2_ID = "8142520"
        val EDI_1 = HerId(EDI1_ID) to "$EDI20-1"
        val EDI_2 = HerId(EDI2_ID) to "$EDI20-2"
        const val MESSAGES_PATH = "messages"
        const val DOK_PATH = "$MESSAGES_PATH/{id}"
        const val HERID = "herId"
        private const val DEFAULT_PING_PATH = ""
    }
}
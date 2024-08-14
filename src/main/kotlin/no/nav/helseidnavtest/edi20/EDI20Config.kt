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

    fun sendURI(b: UriBuilder) = b.path(MESSAGES_PATH).build()

    fun pollURI(b: UriBuilder, herId: HerId, appRec: Boolean) =
        b.path(MESSAGES_PATH)
            .queryParam(INCLUDE_APPREC, "$appRec")
            .queryParam(TO_HER_IDS, herId.verdi)
            .build()

    fun lesURI(b: UriBuilder, id: UUID) = b.path(DOK_PATH).build("$id")

    fun lestURI(b: UriBuilder, id: UUID, herId: HerId) = b.path("$DOK_PATH/read/${herId.verdi}").build("$id")

    fun statusURI(b: UriBuilder, id: UUID) = b.path("$DOK_PATH/status").build("$id")

    fun apprecURI(b: UriBuilder, id: UUID, other: HerId) = b.path("$DOK_PATH/Apprec/${other.verdi}").build("$id")

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val MESSAGES_PATH = "messages"
        const val DOK_PATH = "$MESSAGES_PATH/{id}"
        const val HERID = "herId"
        const val EDI20 = "edi20"
        const val PLAIN = "${EDI20}plain"
        const val VALIDATOR = "8095225"
        const val EDI1_ID = "8142519"
        const val EDI2_ID = "8142520"
        val NAV = HerId(90128)
        val EDI_1 = HerId(EDI1_ID) to "$EDI20-1"
        val EDI_2 = HerId(EDI2_ID) to "$EDI20-2"
        private const val INCLUDE_APPREC = "IncludeAppRec"
        private const val TO_HER_IDS = "ToHerIds"
        private const val DEFAULT_PING_PATH = ""
    }
}
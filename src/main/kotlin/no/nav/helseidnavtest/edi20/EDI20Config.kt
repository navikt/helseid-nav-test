package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.DokumentId
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Ordering.ASC
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.util.*

@ConfigurationProperties(EDI20)
class EDI20Config(baseUri: URI,
                  pingPath: String = DEFAULT_PING_PATH,
                  enabled: Boolean = true,
                  val retries: Int = DEFAULT_RETRIES) :
    AbstractRestConfig(baseUri, pingPath, EDI20, enabled) {

    fun sendURI(b: UriBuilder) = b.path(MESSAGES_PATH).build()

    fun pollURI(b: UriBuilder, params: PollParameters) =
        b.path(MESSAGES_PATH)
            .queryParam(RECEIVER_HER_IDS, params.receiver.verdi)
            .queryParam(SENDER_HER_ID, params.sender?.verdi)
            .queryParam(INCLUDE_APPREC, params.appRec)
            .queryParam(INCLUDE_METADATA, params.metadata)
            .queryParam(BUSINESS_DOKUMENT_ID, params.dokumentId?.verdi)
            .queryParam(MESSAGES_TO_FETCH, params.messages)
            .queryParam(ORDER_BY, params.ordering.value)
            .build()

    fun lesURI(b: UriBuilder, id: UUID) = b.path("${DOK_PATH}/business-dokument").build("$id")

    fun lestURI(b: UriBuilder, id: UUID, herId: HerId) = b.path("$DOK_PATH/read/${herId.verdi}").build("$id")

    fun statusURI(b: UriBuilder, id: UUID) = b.path("$DOK_PATH/status").build("$id")

    fun apprecURI(b: UriBuilder, id: UUID, other: HerId) = b.path("$DOK_PATH/Apprec/${other.verdi}").build("$id")

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    enum class Ordering(val value: Int) {
        ASC(1), DESC(2)
    }

    data class PollParameters(
        val receiver: HerId,
        val appRec: Boolean = false,
        val sender: HerId? = null,
        val metadata: Boolean = true,
        val dokumentId: DokumentId? = null,
        val ordering: Ordering = ASC,
        val messages: Int = 10)

    companion object {
        const val DELEGATING = "delegating"
        private const val DEFAULT_RETRIES = 3
        const val MESSAGES_PATH = "messages"
        const val DOK_PATH = "$MESSAGES_PATH/{id}"
        const val HERID = "herId"
        const val EDI20 = "edi20"
        const val PLAIN = "${EDI20}plain"
        const val VALIDATOR = "8094866"
        const val EDI1_ID = "8142519"
        const val EDI2_ID = "8142520"
        val NAV = HerId(90128)
        val EDI_1 = HerId(EDI1_ID) to "$EDI20-1"
        val EDI_2 = HerId(EDI2_ID) to "$EDI20-2"
        val LEGE = HprId("565501872")
        private const val INCLUDE_APPREC = "IncludeAppRec"
        private const val INCLUDE_METADATA = "IncludeMetadata"
        private const val RECEIVER_HER_IDS = "ReceiverHerIds"
        private const val BUSINESS_DOKUMENT_ID = "BusinessDocumentId"
        private const val SENDER_HER_ID = "SenderHerId"
        private const val MESSAGES_TO_FETCH = "MessagesToFetch"
        private const val ORDER_BY = "OrderBy"
        private const val DEFAULT_PING_PATH = ""
    }
}
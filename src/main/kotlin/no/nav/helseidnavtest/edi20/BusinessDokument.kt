package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import java.util.*

data class BusinessDocument(val businessDocument: String, val properties: Properties = Properties()) {
    data class Properties(
        val system: System = System(),
        val contentTransferEncoding: String = "base64",
        val contentType: String = APPLICATION_XML_VALUE,
        val ebxmlOverrides: EbxmlOverrides? = null
    ) {
        data class EbxmlOverrides(
            val cpaId: String? = null,
            val conversationId: String? = null,
            val service: String? = null,
            val serviceType: String? = null,
            val action: String? = null,
            val senderHerId: Int? = null,
            val senderRole: String? = null,
            val receiverHerId: Int? = null,
            val receiverRole: String? = null
        )

        data class System(
            val applicationName: String = "HelseIdNavTest",
            val applicationVersion: String = "1.0.0",
            val middlewareName: String? = null,
            val middlewareVersion: String? = null
        )
    }
}

data class MessageDTO(val herId: HerId,val messageIds: List<UUID>)

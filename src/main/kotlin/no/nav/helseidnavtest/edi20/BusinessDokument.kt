package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.HerId
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import java.util.*

private const val APP_NAVN = "HelseIdNavTest"
private const val VERSJON = "1.0.0"

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
            val applicationName: String = APP_NAVN,
            val applicationVersion: String = VERSJON,
            val middlewareName: String? = null,
            val middlewareVersion: String? = null
        )
    }
}

data class Messages(val herId: HerId, val messageIds: List<UUID>)

data class Apprec(val result: Int, val properties: ApprecProperties = ApprecProperties(), val errorList: List<ApprecErrorDetail> = emptyList()
) {
    data class ApprecProperties(val system: ApprecSystem = ApprecSystem())
    data class ApprecSystem(val applicationName: String = APP_NAVN, val applicationVersion: String = VERSJON, val middlewareName: String? = null,
                            val middlewareVersion: String? = null)
    data class ApprecErrorDetail(val errorCode: String? = null,  val details: String? = null)
    companion object  {
        val OK = Apprec(1)
    }
}

data class ErrorDetail(
    val errorCode: String ? = null,
    val description: String? = null,
    val oid: String? = null,
    val details: String? = null)

data class Status(
    val herId: HerId,
    val acknowledged: Boolean,
    val appRecReceived: Boolean,
    val appRecResult: Int,
    val appRecErrorList: List<ErrorDetail> = emptyList())

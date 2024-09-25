package no.nav.helseidnavtest.edi20

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.ApprecStatus.OK
import no.nav.helseidnavtest.edi20.PostMessageRequest.SystemInfo
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
import java.time.LocalDateTime
import java.util.*

private const val APP_NAVN = "HelseIdNavTest"
private const val VERSJON = "1.0.0"

data class PostMessageRequest(
    val businessDocument: String,
    val contentTransferEncoding: String = "base64",
    val contentType: String = APPLICATION_XML_VALUE,
    val systemInfo: SystemInfo = SystemInfo(),
    val ebxmlOverrides: EbxmlOverrides? = null) {

    data class SystemInfo(
        val applicationName: String = APP_NAVN,
        val applicationVersion: String = VERSJON,
        val middlewareName: String? = APP_NAVN,
        val middlewareVersion: String? = VERSJON)

    data class EbxmlOverrides(
        val cpaId: String? = null,
        val conversationId: String? = null,
        val service: String? = null,
        val serviceType: String? = null,
        val action: String? = null,
        val senderHerId: Int? = null,
        val senderRole: String? = null,
        val receiverHerId: Int? = null,
        val receiverRole: String? = null)

    override fun toString() =
        "PostMessageRequest(businessDocument='${businessDocument.length}', contentTransferEncoding='$contentTransferEncoding', contentType='$contentType', systemInfo=$systemInfo, ebxmlOverrides=$ebxmlOverrides)"
}

data class Melding(val id: UUID,
                   val receiverHerId: List<HerId>,
                   val senderHerId: HerId?,
                   val businessDocumentId: UUID?,
                   val businessDocumentGenDate: LocalDateTime?,
                   val isApprec: Boolean)

data class ApprecErrorDetail(val description: String, val errorCode: String? = null, val details: String? = null)
enum class ApprecStatus(@JsonValue val result: Int) {
    OK(1),
    ERROR(2),
    DELFEIL(3)
}

data class ApprecRequest(
    val appRecStatus: ApprecStatus = OK,
    val appRecErrorList: List<ApprecErrorDetail> = emptyList(),
    val systemInfo: SystemInfo = SystemInfo())

data class Status(
    val herId: HerId,
    val acknowledged: Boolean,
    val appRecReceived: Boolean,
    val appRecResult: ApprecStatus?,
    val appRecErrorList: List<ApprecErrorDetail> = emptyList())

data class DeftStatus(val receiverDownloadStatus: Map<String, Boolean>)


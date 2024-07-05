package no.nav.helseidnavtest.edi20

data class BusinessDocument(val businessDocument: String, val properties: Properties) {
    data class Properties(val system: System,val ebxmlOverrides: EbxmlOverrides? = null) {
        data class EbxmlOverrides(val cpaId: String ?= null, val conversationId: String? = null, val service: String? = null, val serviceType: String? = null, val action: String? = null, val senderHerId: Int? = null, val senderRole: String? = null, val receiverHerId: Int? = null, val receiverRole: String? = null)
        data class System(val applicationName: String, val applicationVersion: String, val middlewareName: String?=null, val middlewareVersion: String?=null)
    }
}
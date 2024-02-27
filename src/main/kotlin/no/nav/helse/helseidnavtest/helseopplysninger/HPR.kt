package no.nav.helse.helseidnavtest.helseopplysninger

import no.nav.helse.helseidnavtest.helseopplysninger.HPRDetails.*

class HPRDetailsExtractor {

    fun extract(respons: Map<*,*>)  =
        with((respons)) {
            (this["approvals"] as List<*>).map {
                it as Map<*, *>
                HPRDetails(
                    it["profession"] as String,
                    HPRAuthorization(ex(it["authorization"] as Map<String, String>)),
                    HPRRekvisision((it["requisition_rights"] as List<Map<String, String>>).map { ex(it) }),
                    HPRSpesialitet((it["specialities"] as List<Map<String, String>>).map { ex(it) }))
            }
        }
    private fun ex(m: Map<String, String>) = HPRData(m.get("value") as String,m.get("description") as String)
    }

data class HPRDetails(val profession: String, val auth: HPRAuthorization, val rek: HPRRekvisision, val spec: HPRSpesialitet) {
    data class HPRAuthorization(val data: HPRData)
    data class HPRRekvisision(val data: List<HPRData>)
    data class HPRSpesialitet(val data: List<HPRData>)
    data class HPRData(val value: String, val description: String)
}
package no.nav.helse.helseidnavtest.helseopplysninger

class HPRDetailsExtractor {

    fun extract(respons: Any?): List<HPRDetails> {
        val details = (respons as Map<*, *>)
        println(details)
        return (details["approvals"] as List<*>).map { app ->
            app as Map<*, *>
            val prof = app["profession"] as String
            val auth = app["authorization"] as Map<String, String>
            val req = app["requisition_rights"] as List<Map<String, String>>
            val spec = app["specialities"] as List<Map<String, String>>
            val authData =
                HPRDetails.HPRAuthorization(
                    HPRDetails.HPRData(
                        auth["value"].toString(),
                        auth["description"].toString()
                    )
                )
            val rekvData = req.map { ex(it) }.flatten()
            val specData = spec.map { ex(it) }.flatten()
            HPRDetails(
                prof,
                authData,
                HPRDetails.HPRRekvisision(rekvData),
                HPRDetails.HPRSpesialitet(specData)
            ).also {
                println(it)
            }
        }
    }

    private fun ex(m: Map<String, String>) = m.map { (k, v) -> HPRDetails.HPRData(k, v) }
}

data class HPRDetails(
    val profession: String,
    val auth: HPRAuthorization,
    val rek: HPRRekvisision,
    val spec: HPRSpesialitet
) {
    data class HPRAuthorization(val data: HPRData)
    data class HPRRekvisision(val data: List<HPRData>)
    data class HPRSpesialitet(val data: List<HPRData>)
    data class HPRData(val value: String, val description: String)
}
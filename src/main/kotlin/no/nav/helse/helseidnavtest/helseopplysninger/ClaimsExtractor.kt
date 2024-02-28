package no.nav.helse.helseidnavtest.helseopplysninger

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.HPRDetail.HPRAuthorization
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.HPRDetail.HPRData
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.HPRDetail.HPRRekvisision
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.HPRDetail.HPRSpesialitet


class ClaimsExtractor(private val claims : Map<String, Any>) {

    val professions = hprDetails(claims[HPR_DETAILS] as Map<*, *>).professions
    val hprNumber = stringClaim(HPR_NUMBER)
    val securityLevel = stringClaim(SECURITY_LEVEL)
    val assuranceLevel = stringClaim(ASSURANCE_LEVEL)
    fun stringClaim(claim: String) = claims[claim] as String

    private fun hprDetails(respons : Map<*, *>) =
        HPRDetails(with((respons)) {
            (this[APPROVALS] as List<*>).map {
                it as Map<*, *>
                HPRDetail(it[PROFESSION] as String,
                    HPRAuthorization(ex(it[AUTHORIZATION] as Map<String, String>)),
                    HPRRekvisision((it[REQUISITION_RIGHTS] as List<Map<String, String>>).map {r -> ex(r) }),
                    HPRSpesialitet((it[SPECIALITIES] as List<Map<String, String>>).map { s -> ex(s) }))
            }
        })

    data class HPRDetails(val detail : List<HPRDetail>) {

        val professions = detail.map { it.profession }
    }

    data class HPRDetail(val profession : String, val auth : HPRAuthorization, val rek : HPRRekvisision, val spec : HPRSpesialitet) {
        data class HPRAuthorization(val data : HPRData)
        data class HPRRekvisision(val data : List<HPRData>)
        data class HPRSpesialitet(val data : List<HPRData>)
        data class HPRData(val value : String, val description : String)
    }

    companion object {

        fun Authentication.oidcUser() =  principal as OidcUser

        private const val ASSURANCE_LEVEL = "helseid://claims/identity/assurance_level"
        private const val SECURITY_LEVEL = "helseid://claims/identity/security_level"
        private const val HPR_NUMBER = "helseid://claims/hpr/hpr_number"
        private const val APPROVALS = "approvals"
        private const val PROFESSION = "profession"
        private const val AUTHORIZATION = "authorization"
        private const val REQUISITION_RIGHTS = "requisition_rights"
        private const val SPECIALITIES = "specialities"
        private const val HPR_DETAILS = "helseid://claims/hpr/details"

        private fun ex(m : Map<String, String>) = HPRData(m.get("value") as String, m.get("description") as String)
    }
}
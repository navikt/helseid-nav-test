package no.nav.helseidnavtest.security

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import no.nav.helse.helseidnavtest.helseopplysninger.security.ClaimsExtractor.HPRApproval.HPRAutorisasjon
import no.nav.helse.helseidnavtest.helseopplysninger.security.ClaimsExtractor.HPRApproval.HPRData
import no.nav.helse.helseidnavtest.helseopplysninger.security.ClaimsExtractor.HPRApproval.HPRRekvisisjon
import no.nav.helse.helseidnavtest.helseopplysninger.security.ClaimsExtractor.HPRApproval.HPRSpesialitet

@Suppress("UNCHECKED_CAST")
class ClaimsExtractor(private val claims : Map<String,Any>) {

    val professions = claims.get(HPR_DETAILS)?.let { hprDetails(it as Map<*, *>).professions } ?: emptyList()
    val hprNumber = stringClaim(HPR_NUMBER)
    val securityLevel = stringClaim(SECURITY_LEVEL)
    val assuranceLevel = stringClaim(ASSURANCE_LEVEL)
    fun stringClaim(claim: String) = claims[claim] as? String

    private fun hprDetails(respons : Map<*, *>) =
        HPRDetails(with((respons)) {
            (this[APPROVALS] as List<*>).map {
                it as Map<*, *>
                HPRApproval(it[PROFESSION] as String,
                    HPRAutorisasjon(extract(it[AUTHORIZATION] as Map<String, String>)),
                    HPRRekvisisjon((it[REQUISITION_RIGHTS] as List<Map<String, String>>).map(Companion::extract)),
                    HPRSpesialitet((it[SPECIALITIES] as List<Map<String, String>>).map(Companion::extract)))
            }
        })

    data class HPRDetails(val detail : List<HPRApproval>) {

        val professions = detail.map { it.profession }
    }

    data class HPRApproval(val profession : String, val auth : HPRAutorisasjon, val rek : HPRRekvisisjon, val spec : HPRSpesialitet) {
        data class HPRAutorisasjon(val data : HPRData)
        data class HPRRekvisisjon(val data : List<HPRData>)
        data class HPRSpesialitet(val data : List<HPRData>)
        data class HPRData(val value : String, val description : String)
    }

    companion object {

        fun Authentication.oidcUser() =  principal as OidcUser

        private const val ASSURANCE_LEVEL = "helseid://claims/identity/assurance_level"
        private const val SECURITY_LEVEL = "helseid://claims/identity/security_level"
        private const val HPR_NUMBER = "helseid://claims/hpr/hpr_number"
        private const val HPR_DETAILS = "helseid://claims/hpr/hpr_details"
        private const val APPROVALS = "approvals"
        private const val PROFESSION = "profession"
        private const val AUTHORIZATION = "authorization"
        private const val REQUISITION_RIGHTS = "requisition_rights"
        private const val SPECIALITIES = "specialities"
        private const val VALUE = "value"
        private const val DESCRIPTION = "description"

        private fun extract(m : Map<String, String>) = HPRData(m[VALUE] as String, m[DESCRIPTION] as String)
    }
}
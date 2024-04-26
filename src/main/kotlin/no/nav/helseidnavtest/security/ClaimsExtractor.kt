package no.nav.helseidnavtest.security

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nav.helseidnavtest.security.ClaimsExtractor.HPRApproval.*
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser

@Suppress("UNCHECKED_CAST")
class ClaimsExtractor(private val claims : Map<String,Any>) {

    val professions = claims.get(HPR_DETAILS)?.let { hprDetails(it as Map<*, *>).professions } ?: emptyList()
    val hprNumber = claim(HPR_NUMBER).toInt()
    val securityLevel = claim(SECURITY_LEVEL)
    val navn = Navn(claim("given_name"), claim("middle_name"), claim("family_name"))

    val assuranceLevel = claim(ASSURANCE_LEVEL)
    val fnr = Fødselsnummer(claim(PID))

    fun claim(claim: String) = claims[claim] as? String ?: throw IrrecoverableException(INTERNAL_SERVER_ERROR, "Mangler claim $claim", claim)

    private fun hprDetails(respons : Map<*, *>) =
        HPRDetails(with((respons)) {
            (this[APPROVALS] as List<*>).map {
                it as Map<*, *>
                HPRApproval(it[PROFESSION] as String,
                    HPRAutorisasjon(extract(it[AUTHORIZATION] as Map<String, String>)),
                    HPRRekvisisjon((it[REQUISITION_RIGHTS] as List<Map<String, String>>).map(Companion::extract)),
                    HPRSpesialitet((it[SPECIALITIES] as List<Map<String, String>>).map(Companion::extract))
                )
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
        private const val CLAIMS = "helseid://claims/"
        private const val IDENTITY_CLAIM = CLAIMS + "identity/"
        private const val ASSURANCE_LEVEL = IDENTITY_CLAIM +"assurance_level"
        private const val SECURITY_LEVEL =IDENTITY_CLAIM +"security_level"
        private const val PID = IDENTITY_CLAIM +"pid"

        private const val HPR_CLAIM = CLAIMS + "hpr/"
        private const val HPR_NUMBER = HPR_CLAIM + "hpr_number"
        private const val HPR_DETAILS = HPR_CLAIM + "hpr_details"

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

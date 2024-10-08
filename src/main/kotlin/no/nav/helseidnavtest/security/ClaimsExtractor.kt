package no.nav.helseidnavtest.security

import com.nimbusds.openid.connect.sdk.claims.PersonClaims.*
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HprId
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nav.helseidnavtest.security.ClaimsExtractor.HPRApproval.*
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser

@Suppress("UNCHECKED_CAST")
class ClaimsExtractor(private val claims: Map<String, Any>) {
    constructor(user: OidcUser) : this(user.claims)

    data class HelsePersonell(val profession: List<String>,
                              val hprNumber: HprId,
                              val securityLevel: String,
                              val navn: Navn,
                              val assuranceLevel: String,
                              val fnr: Fødselsnummer)

    val professions = claims[HPR_DETAILS]?.let { hprDetails(it as Map<*, *>).professions } ?: emptyList()
    val securityLevel = claim(SECURITY_LEVEL)

    val helsePersonell = HelsePersonell(professions, HprId(claim(HPR_NUMBER)),
        securityLevel,
        Navn(claim(GIVEN_NAME_CLAIM_NAME), claim(MIDDLE_NAME_CLAIM_NAME), claim(FAMILY_NAME_CLAIM_NAME)),
        claim(ASSURANCE_LEVEL),
        Fødselsnummer(claim(PID)))

    private fun claim(claim: String) = claims[claim] as? String
        ?: throw IllegalStateException()

    private fun hprDetails(respons: Map<*, *>) =
        HPRDetails(with((respons)) {
            (this[APPROVALS] as List<*>).map {
                it as Map<*, *>
                HPRApproval(it[PROFESSION] as String,
                    HPRAutorisasjon(extract(it[AUTHORIZATION] as Map<String, String>)),
                    HPRRekvisisjon((it[REQUISITION_RIGHTS] as List<Map<String, String>>).map(::extract)),
                    HPRSpesialitet((it[SPECIALITIES] as List<Map<String, String>>).map(::extract))
                )
            }
        })

    data class HPRDetails(val detail: List<HPRApproval>) {
        val professions = detail.map { it.profession }
    }

    data class HPRApproval(val profession: String,
                           val auth: HPRAutorisasjon,
                           val rek: HPRRekvisisjon,
                           val spec: HPRSpesialitet) {
        data class HPRAutorisasjon(val data: HPRData)
        data class HPRRekvisisjon(val data: List<HPRData>)
        data class HPRSpesialitet(val data: List<HPRData>)
        data class HPRData(val value: String, val description: String)
    }

    companion object {

        fun Authentication.oidcUser() = principal as OidcUser
        private const val CLAIMS = "helseid://claims/"
        private const val IDENTITY_CLAIM = CLAIMS + "identity/"
        private const val HPR_CLAIM = CLAIMS + "hpr/"

        const val APPROVALS = "approvals"
        const val HPR_DETAILS = HPR_CLAIM + "hpr_details"
        internal const val VALUE = "value"
        internal const val DESCRIPTION = "description"
        internal const val PROFESSION = "profession"
        internal const val AUTHORIZATION = "authorization"
        internal const val REQUISITION_RIGHTS = "requisition_rights"
        internal const val SPECIALITIES = "specialities"
        internal const val HPR_NUMBER = HPR_CLAIM + "hpr_number"
        internal const val ASSURANCE_LEVEL = IDENTITY_CLAIM + "assurance_level"
        internal const val SECURITY_LEVEL = IDENTITY_CLAIM + "security_level"
        internal const val PID = IDENTITY_CLAIM + "pid"

        @JvmStatic
        private fun extract(m: Map<String, String>) = HPRData(m[VALUE] as String, m[DESCRIPTION] as String)
    }
}

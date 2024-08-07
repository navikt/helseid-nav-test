package no.nav.helseidnavtest.oppslag.organisasjon

import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder.newInstance
import java.net.URI

@ConfigurationProperties(ORGANISASJON)
class OrganisasjonConfig(
    baseUri: URI, private val organisasjonPath: String = V1_ORGANISASJON,
    @DefaultValue("true") enabled: Boolean = true
) :
    AbstractRestConfig(baseUri, pingPath(organisasjonPath), ORGANISASJON, enabled) {

    fun organisasjonURI(b: UriBuilder, orgnr: OrgNummer) = b.path(organisasjonPath).build(orgnr.verdi)

    companion object {
        const val ORGANISASJON = "organisasjon"
        private const val V1_ORGANISASJON = "v1/organisasjon/{orgnr}"
        private const val TESTORG = "947064649"
        private fun pingPath(organisasjonPath: String) =
            newInstance()
                .path(organisasjonPath)
                .build(TESTORG)
                .toString()
    }

    override fun toString() =
        "${javaClass.simpleName} [organisasjonPath=" + organisasjonPath + ", pingEndpoint=" + pingEndpoint + "]"
}
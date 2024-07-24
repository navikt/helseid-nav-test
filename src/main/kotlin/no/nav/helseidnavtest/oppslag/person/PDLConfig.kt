package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(PDL)
class PDLConfig(
    baseUri: URI,
    pingPath: String = DEFAULT_PING_PATH,
    enabled: Boolean = true
) : AbstractRestConfig(baseUri, pingPath, PDL, enabled) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL = "pdl"
        private const val DEFAULT_PING_PATH = ""
    }
}
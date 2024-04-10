package no.nav.helseidnavtest.oppslag.person

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL

@ConfigurationProperties(PDL)
class PDLConfig(baseUri: URI,
                pingPath: String = DEFAULT_PING_PATH,
                enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, PDL, enabled) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL_CREDENTIALS = "client-credentials-pdl"
        const val PDL = "pdl"
        private const val DEFAULT_PING_PATH = ""
    }
}
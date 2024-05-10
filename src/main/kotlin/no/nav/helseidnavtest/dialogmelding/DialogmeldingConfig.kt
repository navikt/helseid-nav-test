package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.oppslag.AbstractRestConfig

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(DIALOGMELDING)
class DialogmeldingConfig(baseUri: URI, val path: String = PATH,enabled: Boolean = true, pingPath: String = PINGPATH,
                               val request: String, val reply: String) : AbstractRestConfig(baseUri, pingPath, DIALOGMELDING, enabled) {

    companion object {
        const val PATH = "/partner/her/{herid}"
        private  const val PINGPATH = "/internal/health/liveness"
        const val DIALOGMELDING = "dialogmelding"
   }
}
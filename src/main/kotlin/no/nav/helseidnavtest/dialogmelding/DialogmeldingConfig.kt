package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.oppslag.AbstractRestConfig

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(DIALOGMELDING)
 data class DialogmeldingConfig(val uri: URI, val enabled: Boolean = true, val pingPath: String = PINGPATH,
                               val request: String, val reply: String) : AbstractRestConfig(uri, pingPath, DIALOGMELDING, enabled) {

    companion object {
        private  const val PINGPATH = "internal/isAlive"
        const val DIALOGMELDING = "dialogmelding"
   }
}
package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.oppslag.WSConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(DIALOGMELDING)
data class DialogmeldingConfig(val uri: URI, val request: String, val reply: String) : WSConfig(uri) {

    companion object {
        const val DIALOGMELDING = "dialogmelding"
   }
}
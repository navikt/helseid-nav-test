package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(DIALOGMELDING)
data class DialogmeldingConfig(val request: String,val reply: String, val host: String, val channel: String, val qm: String, val port: Int = 1413)  {

    companion object {
        const val DIALOGMELDING = "dialogmelding"
   }
}
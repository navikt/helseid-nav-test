package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(DIALOGMELDING)
class DialogmeldingConfig(val request: String,val response: String, val host: String = "mqls02.preprod.local" +
        "" +
        "", val channel: String, val qm: String, val port: Int = 1413)  {

    override fun toString() = "$javaClass.simpleName [request=$request, response=$response]"

    companion object {
        const val DIALOGMELDING = "dialogmelding"
   }
}
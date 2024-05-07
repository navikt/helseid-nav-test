package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL

@ConfigurationProperties(DIALOGMELDING)
class DialogmeldingConfig(val request: String,val response: String, val host: String)  {

    override fun toString() = "$javaClass.simpleName [request=$request, response=$response]"

    companion object {
        const val DIALOGMELDING = "dialogmelding"
   }
}
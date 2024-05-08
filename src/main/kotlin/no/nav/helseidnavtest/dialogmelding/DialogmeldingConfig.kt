package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBACTION
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBROLE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.Companion.EBSERVICE
import no.nav.helseidnavtest.oppslag.AbstractRestConfig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(DIALOGMELDING)
 data class DialogmeldingConfig(val uri: URI, val enabled: Boolean = true, val pingPath: String = PINGPATH,
                               val request: String, val reply: String) : AbstractRestConfig(uri, pingPath, DIALOGMELDING, enabled) {

    fun query(b: UriBuilder) =
        b.queryParam("service", EBSERVICE)
            .queryParam("role", EBROLE)
            .queryParam("action", EBACTION)
            .build()

    companion object {
        private  const val PINGPATH = "internal/isAlive"
        const val DIALOGMELDING = "dialogmelding"
   }
}
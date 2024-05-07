package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.HelseIdNavTestApplication
import no.nav.helseidnavtest.dialogmelding.ApprecReceiver
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class ApprecReceiver {
    private val log = getLogger(ApprecReceiver::class.java)
    @JmsListener(destination = "#{'\${dialogmelding.reply}'}")
    fun receiveMessage(apprec: XMLAppRec) {
        log.info("Received <$apprec>")
    }
}
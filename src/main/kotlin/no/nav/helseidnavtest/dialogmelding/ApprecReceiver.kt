package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.ApprecReceiver
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
internal class ApprecReceiver {
    private val log = getLogger(ApprecReceiver::class.java)
    @JmsListener(destination = "mailbox")
    fun receiveMessage(apprec: XMLAppRec) {
        log.info("Received <$apprec>")
    }
}
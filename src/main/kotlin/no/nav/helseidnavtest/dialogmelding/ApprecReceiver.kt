package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.ApprecReceiver
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
internal class ApprecReceiver {
    private val log = getLogger(ApprecReceiver::class.java)
    @JmsListener(destination = "QA.Q1_HELSEID.IU03_UTSENDING_REPLY")
    fun receiveMessage(apprec: XMLAppRec) {
        log.info("Received <$apprec>")
    }
}
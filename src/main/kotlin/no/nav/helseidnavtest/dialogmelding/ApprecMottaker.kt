package no.nav.helseidnavtest.dialogmelding

import no.nav.helseopplysninger.apprec.XMLAppRec
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class ApprecMottaker {
    private val log = getLogger(ApprecMottaker::class.java)
    @JmsListener(destination = "#{'\${dialogmelding.reply}'}")
    fun receiveMessage(apprec: XMLAppRec) {
        log.info("Received <$apprec>")
    }
}
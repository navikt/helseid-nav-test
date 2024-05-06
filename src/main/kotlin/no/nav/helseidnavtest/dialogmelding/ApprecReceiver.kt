package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.HelseIdNavTestApplication
import no.nav.helseidnavtest.dialogmelding.ApprecReceiver
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
internal class ApprecReceiver(@Value("\${helseid.emottak.password}") private val pw: String) {
    private val log = getLogger(ApprecReceiver::class.java)
    init {
        log.info("XXXXXX: $pw")
    }
    @JmsListener(destination = "QA.Q1_HELSEID.IU03_UTSENDING_REPLY")
    fun receiveMessage(apprec: XMLAppRec) {
        log.info("Received <$apprec>")
    }
}
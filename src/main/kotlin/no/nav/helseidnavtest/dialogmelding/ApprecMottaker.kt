package no.nav.helseidnavtest.dialogmelding

import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import javax.jms.Message

@Component
class ApprecMottaker {
    private val log = getLogger(ApprecMottaker::class.java)
    @JmsListener(containerFactory = "jmsListenerContainerFactory", destination = "\${dialogmelding.reply}")
    fun receiveMessage(xml: XMLEIFellesformat) {
        val apprec = xml.any.find { it is XMLAppRec } as? XMLAppRec ?: throw IllegalStateException("Ingen apprec i melding")
        log.info("Received <$apprec>")
    }
}
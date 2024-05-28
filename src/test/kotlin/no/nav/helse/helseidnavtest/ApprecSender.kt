package no.nav.helse.helseidnavtest

import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
class ApprecSender(private val template: JmsTemplate, private val cfg: DialogmeldingConfig) {

    private val log = getLogger(ApprecSender::class.java)

    fun send(pasient: Fødselsnummer) {
        val uuid = randomUUID()
        log.info("Sender apprec for pasient $pasient for $cfg")
        template.convertAndSend(cfg.reply, "hello")
    }
}
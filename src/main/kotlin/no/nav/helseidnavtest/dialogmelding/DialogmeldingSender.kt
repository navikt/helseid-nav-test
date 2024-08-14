package no.nav.helseidnavtest.dialogmelding

import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
class DialogmeldingSender(private val template: JmsTemplate,
                          val generator: DialogmeldingGenerator,
                          private val cfg: DialogmeldingConfig,
                          private val lager: Dialogmeldinglager) {

    private val log = getLogger(DialogmeldingSender::class.java)

    fun send(pasient: Fødselsnummer) {
        val uuid = randomUUID()
        //  val melding = generator.genererDialogmelding(pasient, uuid)
        log.info("Sender dialogmelding for pasient $pasient for $cfg")
        //  template.convertAndSend(cfg.request, melding)
        //  lager.lagre(uuid, melding)
    }
}
package no.nav.helseidnavtest.dialogmelding

import org.slf4j.LoggerFactory.getLogger
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class DialogmeldingSender(private val template: JmsTemplate,val generator: DialogmeldingGenerator, private val cfg: DialogmeldingConfig) {

    private val log = getLogger(DialogmeldingSender::class.java)

    fun send(fnr: Fødselsnummer) {
        log.info("Sending dialogmelding for $fnr på ${cfg.request}")
        template.convertAndSend(cfg.request, generator.genererDialogmelding(fnr))
    }
}
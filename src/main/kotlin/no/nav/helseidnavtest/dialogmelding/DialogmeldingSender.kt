package no.nav.helseidnavtest.dialogmelding

import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class DialogmeldingSender(private val template: JmsTemplate) {

    fun send(dialogmelding: String) {
        template.convertAndSend("QA.Q1_HELSEID.IU03_UTSENDING", dialogmelding)
    }
}
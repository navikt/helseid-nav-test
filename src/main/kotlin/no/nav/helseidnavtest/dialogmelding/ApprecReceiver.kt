package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.ApprecReceiver
import no.nav.helseopplysninger.apprec.XMLAppRec
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
internal class ApprecReceiver {
    private val log: `val` = getLogger(ApprecReceiver::ApprecReceiver::)
    @JmsListener(destination = "mailbox")
    fun receiveMessage(apprec: XMLAppRec?): `fun` {
        System.out.println("Received <$email>")
    }
}
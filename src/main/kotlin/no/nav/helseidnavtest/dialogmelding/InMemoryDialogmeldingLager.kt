package no.nav.helseidnavtest.dialogmelding

import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.util.*

@Component
class InMemoryDialogmeldingLager : Dialogmeldinglager {

    private val log = getLogger(InMemoryDialogmeldingLager::class.java)

    override fun lagre(uuid: UUID, dialogmelding: XMLEIFellesformat) {
        log.info("Lagrer dialogmelding $dialogmelding med id $uuid")
    }
}

interface Dialogmeldinglager {

    fun lagre(uuid: UUID, dialogmelding: XMLEIFellesformat)
}

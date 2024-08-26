package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.springframework.stereotype.Component

@Component
class KafkaRecoverer : Recoverer

interface Recoverer {
    fun recover(bestilling: Bestilling) = Unit // TODO
}

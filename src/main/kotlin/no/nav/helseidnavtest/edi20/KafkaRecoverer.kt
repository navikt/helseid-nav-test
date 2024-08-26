package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.springframework.stereotype.Component

@Component
class KafkaRecoverer(private val produsent: RecoverableBestillingProdusent) : Recoverer {
    override fun recover(bestilling: Bestilling) = produsent.send(bestilling)
}

interface Recoverer {
    fun recover(bestilling: Bestilling)
}

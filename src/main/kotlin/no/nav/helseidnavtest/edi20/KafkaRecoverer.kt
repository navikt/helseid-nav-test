package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaRecoverer(private val cfg: BestillingConfig,
                     private val kafkaTemplate: KafkaTemplate<UUID, Bestilling>) : Recoverer {
    override fun recover(bestilling: Bestilling) {
        kafkaTemplate.send(cfg.topics.main, bestilling.id, bestilling)
    }
}

interface Recoverer {
    fun recover(bestilling: Bestilling)
}

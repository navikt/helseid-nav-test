package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.RetryListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaRecoverer(private val cfg: BestillingConfig,
                     private val kafkaTemplate: KafkaTemplate<UUID, Bestilling>) {

    private val log = getLogger(KafkaRecoverer::class.java)

    fun recover(bestilling: Bestilling) =
        with(bestilling) {
            if (cfg.enabled) {
                log.info("Recovering bestilling $id via kafka: $this")
                kafkaTemplate.send(cfg.topics.main, id, this)
            } else {
                log.info("Recovery disabled for bestilling $id")
            }
        }
}

@Component
class LoggingRetryListener : RetryListener {
    private val log = getLogger(RetryListener::class.java)

    override fun failedDelivery(record: ConsumerRecord<*, *>, e: Exception, deliveryAttempt: Int) {
        log.warn("Failed delivery of record $record on attempt $deliveryAttempt", e)
    }

    override fun recovered(record: ConsumerRecord<*, *>, e: Exception) {
        log.info("Recovered record $record", e)
    }

    override fun recoveryFailed(record: ConsumerRecord<*, *>, e: Exception, f: Exception) {
        log.info("Recovery fsiled record $record", e)
    }

}

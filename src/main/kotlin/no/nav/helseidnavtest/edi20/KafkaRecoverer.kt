package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
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
    override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean {
        log.info("open")
        return super.open(context, callback)
    }

    override fun <T : Any?, E : Throwable?> close(context: RetryContext?,
                                                  callback: RetryCallback<T, E>?,
                                                  throwable: Throwable?) {
        log.info("close")
        super.close(context, callback, throwable)
    }

    override fun <T : Any?, E : Throwable?> onSuccess(context: RetryContext?,
                                                      callback: RetryCallback<T, E>?,
                                                      result: T) {
        log.info("onSuccess")
        super.onSuccess(context, callback, result)
    }

    override fun <T : Any?, E : Throwable?> onError(context: RetryContext?,
                                                    callback: RetryCallback<T, E>?,
                                                    throwable: Throwable?) {
        log.info("onError")
        super.onError(context, callback, throwable)
    }
}

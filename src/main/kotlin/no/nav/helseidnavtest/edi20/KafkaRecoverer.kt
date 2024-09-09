package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Innsending
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaRecoverer(private val cfg: InnsendingConfig,
                     private val kafkaTemplate: KafkaTemplate<UUID, Innsending>) : Recoverer {

    private val log = getLogger(KafkaRecoverer::class.java)

    override fun recover(innsending: Innsending) =
        with(innsending) {
            log.info("Recovering innsending $id via kafka: $this")
            kafkaTemplate.send(cfg.topics.main, id, this)
            "$id"
        }
}

interface Recoverer {

    fun recover(innsending: Innsending): String
}

@Component
class LoggingRetryListener : RetryListener {
    private val log = getLogger(RetryListener::class.java)
    override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean {
        log.info("første med retry")
        return super.open(context, callback)
    }

    override fun <T : Any?, E : Throwable?> close(context: RetryContext?,
                                                  callback: RetryCallback<T, E>?,
                                                  t: Throwable?) {
        log.info("ferdig med retry", t)
        super.close(context, callback, t)
    }

    override fun <T : Any?, E : Throwable?> onSuccess(context: RetryContext?,
                                                      callback: RetryCallback<T, E>?,
                                                      result: T) {
        log.info("retry ok")
        super.onSuccess(context, callback, result)
    }

    override fun <T : Any?, E : Throwable?> onError(context: RetryContext?,
                                                    callback: RetryCallback<T, E>?,
                                                    t: Throwable?) {
        log.info("retry feilet", t)
        super.onError(context, callback, t)
    }
}

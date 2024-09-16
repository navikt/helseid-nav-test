package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.oppslag.adresse.Innsending
import org.slf4j.LoggerFactory.getLogger
import org.springframework.kafka.core.KafkaOperations
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaRecoverer(private val cfg: InnsendingConfig,
                     private val kafka: KafkaOperations<UUID, Innsending>) : Recoverer {

    private val log = getLogger(KafkaRecoverer::class.java)

    override fun recover(innsending: Innsending) =
        with(innsending) {
            log.info("Recovering innsending $id via kafka: $this")
            kafka.send(cfg.topics.main, id, this)
            "$id"
        }
}

interface Recoverer {

    fun recover(innsending: Innsending): String
}

@Component
class LoggingRetryListener : RetryListener {
    private val log = getLogger(RetryListener::class.java)
    override fun <T : Any, E : Throwable> open(context: RetryContext, cb: RetryCallback<T, E>): Boolean {
        log.info("Første med retry")
        return super.open(context, cb)
    }

    override fun <T : Any, E : Throwable> close(ctx: RetryContext, cb: RetryCallback<T, E>, t: Throwable) {
        log.info("Ferdig med retry", t)
        super.close(ctx, cb, t)
    }

    override fun <T : Any, E : Throwable> onSuccess(ctx: RetryContext, cb: RetryCallback<T, E>, result: T) {
        log.info("Retry ok")
        super.onSuccess(ctx, cb, result)
    }

    override fun <T : Any, E : Throwable> onError(ctx: RetryContext, cb: RetryCallback<T, E>, t: Throwable?) {
        log.info("Retry feilet", t)
        super.onError(ctx, cb, t)
    }
}

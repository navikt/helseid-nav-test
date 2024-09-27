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
class KafkaRecoverer(private val kafka: KafkaOperations<UUID, Innsending>,
                     private val cfg: InnsendingConfig) : Recoverer {

    private val log = getLogger(KafkaRecoverer::class.java)

    override fun recover(innsending: Innsending) =
        with(innsending) {
            log.info("Recovering innsending $id via kafka: $this")
            kafka.send(cfg.recovery.main, id, this)
            Unit
        }
}

interface Recoverer {

    fun recover(innsending: Innsending)
}

@Component
class LoggingRetryListener : RetryListener {
    private val log = getLogger(RetryListener::class.java)
    override fun <T : Any, E : Throwable> onError(ctx: RetryContext, cb: RetryCallback<T, E>, t: Throwable?) {
        log.info("Retry metode ${ctx.getAttribute("context.name")} feilet for ${ctx.retryCount}. gang", t)
        super.onError(ctx, cb, t)
    }
}

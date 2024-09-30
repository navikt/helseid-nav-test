package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.oppslag.adresse.Innsending
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata
import org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_MESSAGE
import org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_STACKTRACE
import org.springframework.kafka.support.KafkaMessageHeaderAccessor
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
@KafkaListener(topics = ["\${innsending.recovery.main}"], containerFactory = "innsending")
@RetryableTopic(
    retryTopicSuffix = "\${innsending.recovery.retrysuffix}",
    dltTopicSuffix = "\${innsending.recovery.dltsuffix}",
    attempts = "\${innsending.recovery.retries}",
    backoff = Backoff(delayExpression = "\${innsending.recovery.backoff}"),
    exclude = [IrrecoverableException::class],
    traversingCauses = "true",
    autoStartDltHandler = "true",
    autoCreateTopics = "false",
)
class BestillingRetryHendelseKonsument(private val edi: EDI20Service) {

    private val log = LoggerFactory.getLogger(BestillingRetryHendelseKonsument::class.java)

    @KafkaHandler
    fun listen(innsending: Innsending, accessor: KafkaMessageHeaderAccessor, meta: ConsumerRecordMetadata) =
        log.info("Mottatt innsending ${innsending.id} på ${meta.topic()} for ${accessor.nonBlockingRetryDeliveryAttempt} gang.")
            .also { edi.send(innsending) }

    @DltHandler
    fun dlt(innsending: Innsending,
            @Header(DLT_EXCEPTION_MESSAGE) msg: String,
            @Header(DLT_EXCEPTION_STACKTRACE) trace: String) =
        log.error("$msg ${innsending.id} feilet ($trace)")
}
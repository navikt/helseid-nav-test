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
@KafkaListener(topics = ["#{@innsendingConfig.recovery.main}"], containerFactory = InnsendingConfig.INNSENDING)
@RetryableTopic(
    retryTopicSuffix = "#{@innsendingConfig.recovery.retryduffix}",
    dltTopicSuffix = "#{@innsendingConfig.recovery.retryduffix}",
    attempts = "#{@innsendingConfig.recovery.retries}",
    backoff = Backoff(delayExpression = "#{@innsendingConfig.recovery.backoff}"),
    exclude = [IrrecoverableException::class],
    traversingCauses = "true",
    autoStartDltHandler = "true",
    autoCreateTopics = "false",
)
class BestillingHendelseKonsument(private val edi: EDI20Service, val cfg: InnsendingConfig) {

    private val log = LoggerFactory.getLogger(BestillingHendelseKonsument::class.java)

    @KafkaHandler
    fun listen(innsending: Innsending,
               accessor: KafkaMessageHeaderAccessor,
               meta: ConsumerRecordMetadata) =
        log.info("Mottatt innsending ${innsending.id} på ${meta.topic()} for ${accessor.nonBlockingRetryDeliveryAttempt.let { "$it." }} gang.")
            .also { edi.send(innsending) }

    @DltHandler
    fun dlt(innsending: Innsending,
            @Header(DLT_EXCEPTION_MESSAGE) msg: String,
            @Header(DLT_EXCEPTION_STACKTRACE) trace: String) =
        log.error("$msg ${innsending.id}  ${cfg.recovery.retries} forsøk ($trace)")
}
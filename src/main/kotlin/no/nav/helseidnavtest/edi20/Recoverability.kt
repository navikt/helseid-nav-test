package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.InnsendingConfig.Companion.INNSENDING
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.oppslag.adresse.Innsending
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_MESSAGE
import org.springframework.kafka.support.KafkaHeaders.DLT_EXCEPTION_STACKTRACE
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(INNSENDING)
data class InnsendingConfig(@NestedConfigurationProperty val topics: InnsendingTopics = InnsendingTopics(),
                            val enabled: Boolean = true) : KafkaConfig(INNSENDING, enabled) {

    override fun topics() = topics.all

    data class InnsendingTopics(
        val retry: String = RETRY_TOPIC,
        val dlt: String = DLT_TOPIC,
        val main: String = MAIN_TOPIC,
        val backoff: Int = DEFAULT_BACKOFF,
        val retries: Int = DEFAULT_RETRIES
    ) {

        val all = listOf(main, retry, dlt)
    }

    companion object {

        private const val PREFIX = "helseopplysninger.edi20."
        const val INNSENDING = "bestilling"
        private const val DEFAULT_BACKOFF = 30000
        private const val DEFAULT_RETRIES = 3
        private const val MAIN_TOPIC = PREFIX + "main"
        private const val RETRY_TOPIC = PREFIX + "retry"
        private const val DLT_TOPIC = PREFIX + "dlt"
    }
}

abstract class KafkaConfig(val name: String, val isEnabled: Boolean) {

    abstract fun topics(): List<String>
}

@Component
class InnsendingRetryTopicNamingProviderFactory(private val cf: InnsendingConfig) : RetryTopicNamesProviderFactory {

    override fun createRetryTopicNamesProvider(p: Properties): RetryTopicNamesProvider {
        with(cf.topics) {
            if (p.isDltTopic) {
                return object : SuffixingRetryTopicNamesProvider(p) {
                    override fun getTopicName(topic: String) = dlt
                }
            }
            if (p.isMainEndpoint) {
                return object : SuffixingRetryTopicNamesProvider(p) {
                    override fun getTopicName(topic: String) = topic
                }
            }
            return object : SuffixingRetryTopicNamesProvider(p) {
                override fun getTopicName(topic: String) = retry
            }
        }
    }
}

@Component
class BestillingHendelseKonsument(private val edi: EDI20Service, val cfg: InnsendingConfig) {

    private val log = getLogger(BestillingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{@innsendingConfig.topics.main}"], containerFactory = INNSENDING)
    @RetryableTopic(attempts = "#{@innsendingConfig.topics.retries}",
        backoff = Backoff(delayExpression = "#{@innsendingConfig.topics.backoff}"),
        exclude = [IrrecoverableException::class],
        autoStartDltHandler = "true",
        autoCreateTopics = "false")
    fun listen(innsending: Innsending) = edi.send(innsending)

    @DltHandler
    fun dlt(innsending: Innsending,
            @Header(DLT_EXCEPTION_MESSAGE) msg: String,
            @Header(DLT_EXCEPTION_STACKTRACE) trace: String) =
        log.error("$msg ${innsending.id}  ${cfg.topics.retries} forsøk ($trace)")
}

package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.BestillingConfig.Companion.BESTILLING
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
import org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(BESTILLING)
data class BestillingConfig(@NestedConfigurationProperty val topics: BestillingTopics = BestillingTopics(),
                            val enabled: Boolean = true) : KafkaConfig(BESTILLING, enabled) {

    override fun topics() = topics.all

    data class BestillingTopics(
        val retry: String = RETRY_TOPIC,
        val dlt: String = DLT_TOPIC,
        val main: String = MAIN_TOPIC,
        val backoff: Int = DEFAULT_BACKOFF,
        val retries: Int = DEFAULT_RETRIES
    ) {

        val all = listOf(main, retry, dlt)
    }

    companion object {

        const val BESTILLING = "bestilling"
        private const val DEFAULT_BACKOFF = 30000
        private const val DEFAULT_RETRIES = 24
        private const val MAIN_TOPIC = "helseopplysninger.edi20.main"
        private const val RETRY_TOPIC = "helseopplysninger.edi20.retry"
        private const val DLT_TOPIC = "helseopplysninger.edi20.dlt"
    }
}

abstract class KafkaConfig(val name: String, val isEnabled: Boolean) {

    abstract fun topics(): List<String>
}

@Component
class BestillingRetryTopicNamingProviderFactory(private val cf: BestillingConfig) : RetryTopicNamesProviderFactory {

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
class BestillingHendelseKonsument(private val edi: EDI20Service, val cfg: BestillingConfig) {

    private val log = getLogger(BestillingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{@bestillingConfig.topics.main}"], containerFactory = BESTILLING)
    @RetryableTopic(attempts = "#{@bestillingConfig.topics.retries}",
        backoff = Backoff(delayExpression = "#{@bestillingConfig.topics.backoff}"),
        sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
        exclude = [IrrecoverableException::class],
        dltStrategy = FAIL_ON_ERROR,
        autoStartDltHandler = "true",
        autoCreateTopics = "false")
    fun listen(bestilling: Bestilling, @Header(DEFAULT_HEADER_ATTEMPTS, required = false) antall: Int?,
               @Header(RECEIVED_TOPIC) topic: String) =
        log.info("Retrying bestilling ${bestilling.id} on topic $topic").also {
            edi.send(bestilling)
        }

    @DltHandler
    fun dlt(bestilling: Bestilling) =
        log.error("Gir opp bestilling ${bestilling.id} etter ${cfg.topics.retries} forsøk")
}

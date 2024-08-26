package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.BestillingConfig.Companion.BESTILLING
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
import org.springframework.stereotype.Component
import java.util.*

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

        val all = listOf(retry, dlt)
    }

    companion object {

        const val BESTILLING = "bestilling"
        private const val DEFAULT_BACKOFF = 30000
        private const val DEFAULT_RETRIES = 24
        private const val MAIN_TOPIC = "edi20.main"
        private const val RETRY_TOPIC = "edi20.retry"
        private const val DLT_TOPIC = "edi20.dlt"
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
class RecoverableBestillingProdusent(private val cfg: BestillingConfig,
                                     private val kafkaTemplate: KafkaTemplate<UUID, Bestilling>) {

    private val log = getLogger(RecoverableBestillingProdusent::class.java)

    fun send(bestiling: Bestilling) {
        log.info("Sender bestilling $bestiling")
        kafkaTemplate.send(cfg.topics.main, bestiling.id, bestiling)
    }
}

@Component
class BestillingHendelseKonsument(private val cfg: BestillingConfig) {

    private val log = getLogger(BestillingHendelseKonsument::class.java)

    @KafkaListener(topics = ["#{'\${bestilling.topics.main}'}"], containerFactory = BESTILLING)

    fun listen(bestilling: Bestilling) {
        log.info("Retrying bestilling $bestilling")

    }
}
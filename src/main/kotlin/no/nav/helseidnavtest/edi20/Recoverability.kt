package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.InnsendingConfig.Companion.INNSENDING
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.kafka.retrytopic.DestinationTopic.Properties
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory.RetryTopicNamesProvider
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory.SuffixingRetryTopicNamesProvider
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

//@Component
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


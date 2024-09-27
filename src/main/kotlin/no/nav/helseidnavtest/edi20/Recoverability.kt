package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.InnsendingConfig.Companion.INNSENDING
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(INNSENDING)
data class InnsendingConfig(@NestedConfigurationProperty val recovery: InnsendingTopics = InnsendingTopics(),
                            val enabled: Boolean = true) : KafkaConfig(INNSENDING, enabled) {

    override fun topics() = recovery.all

    data class InnsendingTopics(
        val main: String = MAIN_TOPIC,
        val retrysuffix: String = RETRY_SUFFIX,
        val dltsuffix: String = DLT_SUFFIX,
        val backoff: Int = DEFAULT_BACKOFF,
        val retries: Int = DEFAULT_RETRIES
    ) {

        val all = listOf(main, main + retrysuffix, main + dltsuffix)
    }

    companion object {

        private const val PREFIX = "helseopplysninger.edi20."
        const val INNSENDING = "innsending"
        private const val DEFAULT_BACKOFF = 30000
        private const val DEFAULT_RETRIES = 3
        private const val MAIN_TOPIC = PREFIX + "main"
        private const val RETRY_SUFFIX = ".retry"
        private const val DLT_SUFFIX = ".dlt"
    }
}

abstract class KafkaConfig(val name: String, val isEnabled: Boolean) {

    abstract fun topics(): List<String>
}



package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.edi20.InnsendingConfig.Companion.INNSENDING
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(INNSENDING)
data class InnsendingConfig(@NestedConfigurationProperty val recovery: Recovery,
                            val enabled: Boolean = true) : KafkaConfig(INNSENDING, enabled) {

    override fun topics() = recovery.all

    data class Recovery(
        val main: String,
        val retrysuffix: String,
        val dltsuffix: String,
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
    }
}

abstract class KafkaConfig(val name: String, val isEnabled: Boolean) {

    abstract fun topics(): List<String>
}



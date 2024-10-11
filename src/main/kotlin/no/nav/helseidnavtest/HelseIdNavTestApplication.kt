package no.nav.helseidnavtest

import no.nav.boot.conditionals.Cluster.Companion.profiler
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.jms.annotation.EnableJms
import org.springframework.kafka.annotation.EnableKafkaRetryTopic
import org.springframework.modulith.core.ApplicationModules
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebSecurity
@EnableWebMvc
@EnableMethodSecurity
@ConfigurationPropertiesScan
@EnableRetry(proxyTargetClass = true)
@EnableCaching
@EnableJms
@EnableScheduling
@EnableKafkaRetryTopic
class HelseIdNavTestApplication

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger(HelseIdNavTestApplication::class.java)

    ApplicationModules.of(HelseIdNavTestApplication::class.java).verify().forEach { log.info(it.toString()) }
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}
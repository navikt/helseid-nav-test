package no.nav.helseidnavtest
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.helseidnavtest.oppslag.person.PDLController
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.jms.annotation.EnableJms
import org.springframework.retry.annotation.EnableRetry
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebSecurity
@EnableWebMvc
@ConfigurationPropertiesScan
@EnableRetry
@EnableJms
class HelseIdNavTestApplication

@Value("\${helseid.emottak.password}")
lateinit var pw: String

private val log = getLogger(HelseIdNavTestApplication::class.java)


fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        log.info("XXXXXX: $pw")
        setAdditionalProfiles(*profiler())
    }
}
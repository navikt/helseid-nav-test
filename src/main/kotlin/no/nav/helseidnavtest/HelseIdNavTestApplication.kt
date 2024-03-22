package no.nav.helseidnavtest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.helse.helseidnavtest.helseopplysninger.adapters.LocalDateAdapter
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConfigurationPropertiesScan
@EnableRetry
class HelseIdNavTestApplication

fun main(args: Array<String>) {
    runApplication<no.nav.helseidnavtest.HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}
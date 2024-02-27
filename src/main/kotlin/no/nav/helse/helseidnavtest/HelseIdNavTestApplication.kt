package no.nav.helse.helseidnavtest

import no.nav.boot.conditionals.Cluster.Companion.profiler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class HelseIdNavTestApplication

fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}
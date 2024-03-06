package no.nav.helse.helseidnavtest

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.helse.helseidnavtest.helseopplysninger.fastlege.FastlegeWSAdapter

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConfigurationPropertiesScan
class HelseIdNavTestApplication : CommandLineRunner {

    val log = LoggerFactory.getLogger(HelseIdNavTestApplication::class.java)

    @Autowired
    lateinit var applicationContext : ApplicationContext
    override fun run(vararg args : String) {
        try {
            applicationContext.getBean(FastlegeWSAdapter::class.java).fastlege(7125186  , "19087999648").also {
                log.info("Fastlege: $it")
            }
        } catch (e: Exception) {
            log.warn("Feil ved sjekk av fastlege",e)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}
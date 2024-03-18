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
import no.nav.helse.helseidnavtest.helseopplysninger.adresse.AdresseWSAdapter
import no.nav.helse.helseidnavtest.helseopplysninger.fastlege.FastlegeWSAdapter
import no.nav.helse.helseidnavtest.security.SecurityConfig

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConfigurationPropertiesScan
class HelseIdNavTestApplication : CommandLineRunner {

    val log = LoggerFactory.getLogger(HelseIdNavTestApplication::class.java)

    @Autowired
    lateinit var ctx : ApplicationContext
    override fun run(vararg args : String) {
        try {

            ctx.getBean(AdresseWSAdapter::class.java).apply {
                ping().also {
                    log.info("ping respons: $it")
                }
            }
            ctx.getBean(FastlegeWSAdapter::class.java).apply {
                fastlege(7125186  , "19087999648").also {
                    log.info("Fastlegestatus : $it")
                }
                fastlegeForPasient("19087999648").also {
                    log.info("Fastlege herID: ${it.patientNIN.value} ${it.gpHerId.value}")
                }
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
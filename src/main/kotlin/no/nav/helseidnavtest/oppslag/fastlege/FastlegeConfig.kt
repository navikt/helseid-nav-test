package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.oppslag.WSConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.net.URI.*

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : WSConfig(create(url),username,password)
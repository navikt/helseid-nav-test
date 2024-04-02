package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.oppslag.WSConfig
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : WSConfig(url,username,password)
package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.oppslag.BasicAuthConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI.create

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : BasicAuthConfig(create(url), username, password)
package no.nav.helseidnavtest.oppslag.fastlege

import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.WSConfig

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : WSConfig(url,username,password)
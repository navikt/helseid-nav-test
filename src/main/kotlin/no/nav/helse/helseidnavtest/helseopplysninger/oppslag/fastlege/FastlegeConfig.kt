package no.nav.helse.helseidnavtest.helseopplysninger.fastlege

import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helse.helseidnavtest.helseopplysninger.WSConfig

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : WSConfig(url,username,password)
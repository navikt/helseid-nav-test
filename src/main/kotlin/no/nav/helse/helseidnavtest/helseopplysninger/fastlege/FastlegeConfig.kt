package no.nav.helse.helseidnavtest.helseopplysninger.fastlege

import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helse.helseidnavtest.helseopplysninger.FastlegeConfig

@ConfigurationProperties("fastlege")
class FastlegeConfig(url: String, username: String, password: String) : FastlegeConfig(url,username,password)
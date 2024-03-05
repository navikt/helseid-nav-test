package no.nav.helse.helseidnavtest.helseopplysninger.fastlege

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("fastlege")
data class FastlegeConfig(val url: String, val username: String, val password: String)
package no.nav.helse.helseidnavtest.helseopplysninger.adresse

import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helse.helseidnavtest.helseopplysninger.FastlegeConfig

@ConfigurationProperties("adresse")
class AdresseConfig(url: String, username: String, password: String) : FastlegeConfig(url,username,password)
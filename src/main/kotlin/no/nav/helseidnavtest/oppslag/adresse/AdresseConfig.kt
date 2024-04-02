package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.oppslag.WSConfig
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("adresse")
class AdresseConfig(url: String, username: String, password: String) : WSConfig(url,username,password)
package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.oppslag.WSConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("adresse")
class AdresseRegisterConfig(url: String, username: String, password: String) : WSConfig(URI.create(url),username,password)
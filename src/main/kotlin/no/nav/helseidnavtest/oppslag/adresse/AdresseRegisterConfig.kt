package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.oppslag.BasicAuthConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("adresse")
class AdresseRegisterConfig(url: String, username: String, password: String) : BasicAuthConfig(URI.create(url),username,password)
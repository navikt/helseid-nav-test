package no.nav.helseidnavtest.oppslag.adresse

import org.springframework.boot.context.properties.ConfigurationProperties
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.WSConfig
@ConfigurationProperties("adresse")
class AdresseConfig(url: String, username: String, password: String) : WSConfig(url,username,password)
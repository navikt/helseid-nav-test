package no.nav.helseidnavtest.oppslag

import org.slf4j.LoggerFactory.getLogger
import java.net.URI

abstract class AbstractRestConfig(val baseUri: URI,
                                  private val pingPath: String,
                                  name: String = baseUri.host,
                                  isEnabled: Boolean) : AbstractConfig(name, isEnabled) {

    protected val log = getLogger(javaClass)
    val pingEndpoint = uri(baseUri, pingPath)
    override fun toString() = "name=$name, pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri"
}

abstract class AbstractConfig(val name: String, val isEnabled: Boolean)
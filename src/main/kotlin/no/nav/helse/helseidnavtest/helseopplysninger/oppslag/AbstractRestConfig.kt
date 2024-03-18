package no.nav.helse.helseidnavtest.helseopplysninger.oppslag

import java.net.URI

abstract class AbstractRestConfig(val baseUri : URI, private val pingPath : String, name : String = baseUri.host, isEnabled : Boolean) : AbstractConfig(name, isEnabled) {

    val pingEndpoint = uri(baseUri, pingPath)

    override fun toString() = "name=$name, pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri"
}

abstract class AbstractConfig(val name : String, val isEnabled : Boolean)
package no.nav.helse.helseidnavtest.helseopplysninger.health

interface Pingable {

    fun ping() : Map<String, String>
    fun pingEndpoint() : String
    fun name() : String
    fun isEnabled() : Boolean
}
package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

interface Pingable {

    fun ping() : Map<String, String>
    fun pingEndpoint() : String
    fun name() : String
    fun isEnabled() : Boolean
}
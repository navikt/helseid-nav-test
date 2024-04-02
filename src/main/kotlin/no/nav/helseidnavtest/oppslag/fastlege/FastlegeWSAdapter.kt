package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.flr.IFlrReadOperations
import org.springframework.stereotype.Component
import java.util.*
import javax.xml.datatype.DatatypeFactory.*

@Component
class FastlegeWSAdapter(private val cfg: FastlegeConfig) : Pingable {

    private val client = createPort<IFlrReadOperations>(cfg)

    fun bekreftFastlege(hpr: Int, fnr: Fødselsnummer) = client.confirmGP(fnr.fnr, hpr, now())

    fun fastlegeForPasient(fnr: Fødselsnummer) = client.getPatientGPDetails(fnr.fnr)

    fun detaljer(fnr: Fødselsnummer) = client.getPatientGPDetails(fnr.fnr)


    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = cfg.url
}
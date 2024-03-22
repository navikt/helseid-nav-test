package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helse.helseidnavtest.helseopplysninger.health.Pingable
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.arbeid.Fødselsnummer
import java.util.Date
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory.*
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.oppslag.createPort
import no.nav.helse.helseidnavtest.ws.flr.IFlrReadOperations

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
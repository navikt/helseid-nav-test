package no.nav.helse.helseidnavtest.helseopplysninger.fastlege

import java.util.Date
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory.*
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.helseopplysninger.createPort
import no.nav.helse.helseidnavtest.ws.flr.IFlrReadOperations

@Component
class FastlegeWSAdapter(cfg: FastlegeConfig) {

    private val client = createPort<IFlrReadOperations>(cfg)

    fun fastlege(hpr: Int, fnr: String) = client.confirmGP(fnr, hpr, now())

    fun fastlegeForPasient(fnr: String) = client.getPatientGPDetails(fnr)


    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
}
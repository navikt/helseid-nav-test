package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.flr.IFlrReadOperations
import no.nav.helseidnavtest.ws.flr.WSGPOffice
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.util.*
import javax.xml.datatype.DatatypeFactory.*

@Component
class FastlegeWSAdapter(private val cfg: FastlegeConfig) : Pingable {

    private val log = getLogger(javaClass)

    private val client = createPort<IFlrReadOperations>(cfg)

    fun herId(pasient: String) = kotlin.runCatching {
        HerId(client.getPatientGPDetails(pasient).gpHerId.value)
    }.getOrElse {
        log.error("Fant ikke fastlege for pasient $pasient", it)
        throw it
    }

    fun bekreftFastlege(hpr: Int, pasient: Fødselsnummer) = client.confirmGP(pasient.value, hpr, now())

    fun kontor(pasient: Fødselsnummer) =
        with(client.getPatientGPDetails(pasient.value)) {
            with(gpContract.value) {
              kontor(gpOffice.value, gpOfficeOrganizationNumber)
            }
        }

    private fun kontor(kontor: WSGPOffice, orgnr: Int) =
        with(kontor) {
            with(physicalAddresses.value.physicalAddress.first()) {
                BehandlerKontor(name.value, streetAddress.value,
                   Postnummer(postalCode), city.value, Virksomhetsnummer(orgnr))
            }
        }

    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
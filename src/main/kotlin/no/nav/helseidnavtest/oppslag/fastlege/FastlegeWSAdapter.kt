package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.BehandlerKontor
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.PartnerId
import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.flr.IFlrReadOperations
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.lang.String.format
import java.util.*
import javax.xml.datatype.DatatypeFactory.*

@Component
class FastlegeWSAdapter(private val cfg: FastlegeConfig) : Pingable {

    private val log = getLogger(javaClass)


    private val client = createPort<IFlrReadOperations>(cfg)

    fun fastlegeHerId(pasient: Fødselsnummer) = client.getPatientGPDetails(pasient.value).gpHerId.value

    fun bekreftFastlege(hpr: Int, pasient: Fødselsnummer) = client.confirmGP(pasient.value, hpr, now())

    fun kontor(pasient: Fødselsnummer) =
        with(client.getPatientGPDetails(pasient.value)) {
            log.info("Legens herid: {} for pasient ${pasient.value}", gpHerId.value)
            with(gpContract.value) {
                BehandlerKontor(
                    partnerId = PartnerId(42),
                    navn = gpOffice.value.name.value,
                    orgnummer = Virksomhetsnummer(gpOfficeOrganizationNumber),
                    postnummer = gpOffice.value.physicalAddresses.value.physicalAddress.first().postalCode.postcode(),
                    poststed = gpOffice.value.physicalAddresses.value.physicalAddress.first().city.value,
                    adresse = gpOffice.value.physicalAddresses.value.physicalAddress.first().streetAddress.value
                )
            }

        }

    private fun Int.postcode() = format("%04d", this)

    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
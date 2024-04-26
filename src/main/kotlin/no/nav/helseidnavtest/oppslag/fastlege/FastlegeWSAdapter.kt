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

    fun bekreftFastlege(hpr: Int, fnr: Fødselsnummer) = client.confirmGP(fnr.value, hpr, now())

    fun kontor(fnr: Fødselsnummer) =
        with(client.getPatientGPDetails(fnr.value)) {
            gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.forEach{ log.info("{}", it) }
            BehandlerKontor(
                partnerId = PartnerId(42),
               // herId = herId, //  gpHerId.value, // TODO Dette er legen, ikke kontoret
                navn = gpContract.value.gpOffice.value.name.value,
                orgnummer = Virksomhetsnummer(gpContract.value.gpOfficeOrganizationNumber),
                postnummer = gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.first().postalCode.postcode(),
                poststed = gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.first().city.value,
                adresse = gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.first().streetAddress.value
            )
        }

    private fun Int.postcode() = format("%04d", this)

    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
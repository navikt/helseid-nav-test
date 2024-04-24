package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.BehandlerKontor
import no.nav.helseidnavtest.dialogmelding.PartnerId
import no.nav.helseidnavtest.dialogmelding.Virksomhetsnummer
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

    fun kontor(fnr: Fødselsnummer) =
        with(client.getPatientGPDetails(fnr.fnr)) {
            BehandlerKontor(
                partnerId = PartnerId(42),
                herId = gpHerId.value,
                navn = gpContract.value.gpOffice.value.name.value,
                orgnummer = Virksomhetsnummer(gpContract.value.gpOfficeOrganizationNumber),
                postnummer = gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.first().postalCode.postcode(),
                poststed = gpContract.value.gpOffice.value.physicalAddresses.value.physicalAddress.first().city.value,
                adresse = "Gata 1"
            )
        }

    private fun Int.postcode() = if (this < 1000) "0" + toString() else toString()

    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
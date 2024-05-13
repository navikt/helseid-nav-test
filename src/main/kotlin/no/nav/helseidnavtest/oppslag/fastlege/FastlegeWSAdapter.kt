package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.ws.flr.IFlrReadOperations
import no.nav.helseidnavtest.ws.flr.IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage
import no.nav.helseidnavtest.ws.flr.WSGPOffice
import org.springframework.stereotype.Component

@Component
class FastlegeWSAdapter(val cfg: FastlegeConfig) : Pingable {


    private val client = createPort<IFlrReadOperations>(cfg)

    fun herId(pasient: String) = runCatching {
        HerId(client.getPatientGPDetails(pasient).gpHerId.value)
    }.getOrElse {
        when (it) {
            is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException(it.message,uri = cfg.url,detail = "Fant ikke fastlege for pasient $pasient" ,cause = it)
            else -> throw it
        }
    }

    fun kontor(pasient: Fødselsnummer) =
        runCatching {
            with(client.getPatientGPDetails(pasient.verdi)) {
                with(gpContract.value) {
                    kontor(gpOffice.value, gpOfficeOrganizationNumber)
                }
            }
        }.getOrElse {
            when (it) {
                is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException(it.message,uri = cfg.url,detail = "Fant ikke detaljer for pasient $pasient" ,cause = it)
                else -> throw it
            }
        }

    private fun kontor(kontor: WSGPOffice, orgnr: Int) =
        with(kontor) {
            with(physicalAddresses.value.physicalAddress.first()) {
                BehandlerKontor(name.value, streetAddress.value,
                   Postnummer(postalCode), city.value, Orgnummer(orgnr))
            }
        }
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
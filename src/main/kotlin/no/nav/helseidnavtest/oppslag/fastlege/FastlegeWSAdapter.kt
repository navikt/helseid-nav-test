package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nhn.schemas.reg.flr.IFlrReadOperations
import no.nhn.schemas.reg.flr.IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage
import no.nhn.schemas.reg.flr.WSGPOffice
import org.springframework.stereotype.Component

@Component
class FastlegeWSAdapter(val cfg: FastlegeConfig) : Pingable {

    private val client = createPort<IFlrReadOperations>(cfg)

    fun herIdForLegeViaPasient(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient).gpHerId.value
    }.getOrElse {
        when (it) {
            is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Fant ikke fastlege for pasient $pasient",it.message, cfg.url, it)
            else -> throw it
        }
    }

    fun kontorViaPasient(pasient: String) =
        runCatching {
            with(client.getPatientGPDetails(pasient)) {
                with(gpContract.value) {
                    fastlegeKontor(gpOffice.value, gpOfficeOrganizationNumber)
                }
            }
        }.getOrElse {
            when (it) {
                is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Fant ikke detaljer for pasient $pasient", it.message ,cfg.url,it)
                else -> throw it
            }
        }

    private fun fastlegeKontor(kontor: WSGPOffice, orgnr: Int) =
        with(kontor) {
            with(physicalAddresses.value.physicalAddress.first()) {
                BehandlerKontor(name.value, streetAddress.value,
                   Postnummer(postalCode), city.value, Orgnummer(orgnr))
            }
        }
    override fun ping() = emptyMap<String,String>()
    override fun pingEndpoint() = "${cfg.url}"
}
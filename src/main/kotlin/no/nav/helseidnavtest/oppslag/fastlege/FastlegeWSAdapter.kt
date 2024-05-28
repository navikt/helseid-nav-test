package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterWSAdapter
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.oppslag.person.Person
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nhn.schemas.reg.flr.IFlrReadOperations
import no.nhn.schemas.reg.flr.IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage
import no.nhn.schemas.reg.flr.WSGPOffice
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class FastlegeWSAdapter(val cfg: FastlegeConfig) : Pingable {

    private val log = getLogger(FastlegeWSAdapter::class.java)


    private val client = createPort<IFlrReadOperations>(cfg)

    fun herIdForLegeViaPasient(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient).gpHerId.value
    }.getOrElse {
        when (it) {
            is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Fant ikke fastlege for pasient $pasient",it.message, cfg.url, it)
            else -> throw it
        }
    }

    fun lege(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient)?.let { d ->
            log.info("Detaljer for pasient $pasient: $d")
            d.doctorCycles?.let { c ->
                log.info("Sykler for pasient $pasient: $c")
                c.value?.let { a ->
                    log.info("Assosiasjoner for pasient $pasient: $a")
                    a.gpOnContractAssociation?.map {
                        log.info("Assosiasjon for pasient $pasient: $it")
                        it.gp.value?.let {
                            log.info("GP for pasient $pasient: ${it.nin.value}")
                            with(it)  {
                                log.info("GP FNR for pasient $pasient: ${this.nin.value}")
                                Lege(Fødselsnummer(nin.value),Navn(firstName.value, middleName.value,lastName.value))
                            }
                        }?: throw NotFoundException("Fant ikke GP for pasient $pasient", uri=cfg.url)
                    } ?: throw NotFoundException("Fant ikke kontraktassosiasjon for pasient $pasient", uri=cfg.url)
                } ?: throw NotFoundException("Fant ikke kontraktassosiasjoner for pasient $pasient", uri=cfg.url)
            } ?: throw NotFoundException("Fant ikke doktorsykler for pasient $pasient", uri=cfg.url)
        } ?: throw NotFoundException("Fant ikke GP detaljer for pasient $pasient", uri=cfg.url)
    }.getOrElse {
        when (it) {
            is IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage -> throw NotFoundException("Fant ikke fastlege for pasient $pasient",it.message, cfg.url, it)
            else -> throw it
        }
    }

     data class Lege(val fnr: Fødselsnummer, val navn: Navn)

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
package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.createPort
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nhn.schemas.reg.flr.IFlrReadOperations
import no.nhn.schemas.reg.flr.IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage as ReadFault
import no.nhn.schemas.reg.flr.ObjectFactory
import no.nhn.schemas.reg.flr.WSGPOffice
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class FastlegeWSAdapter(val cfg: FastlegeConfig) : Pingable {

    private val log = getLogger(FastlegeWSAdapter::class.java)
    private val client = createPort<IFlrReadOperations>(cfg)

    fun pasienterForFastlege(navn: String) = runCatching {
        val search = OF.createWSGPSearchParameters().apply {
            fullText = OF.createWSGPContractQueryParametersFullText(navn)
            page = 1
            pageSize = 10
        }
        client.searchForGP(search).results.value.gpDetails.map {d ->
            val lege = with(d.gp.value) {
                Person(Fødselsnummer(nin.value),
                    Navn(firstName.value, middleName.value, lastName.value))
            }
            val x = d.contracts?.value?.gpOnContractAssociation?.map {a ->
                 a.gpContract.value.patientList.value?.patientToGPContractAssociation?.map {l ->
                    with(l.patient.value) {
                        Person(Fødselsnummer(nin.value), Navn(firstName.value, middleName.value, lastName.value))
                    }
                } ?: emptyList()
            }?.flatten() ?: emptyList()
            LegeListe(lege, x)
        }
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege med $navn",it.message, cfg.url, it)
            else -> throw it
        }
    }

    fun herIdForLegeViaPasient(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient).gpHerId.value
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege for pasient $pasient",it.message, cfg.url, it)
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
                                Lege(d.gpContractId,Person(Fødselsnummer(nin.value),Navn(firstName.value, middleName.value,lastName.value)))
                            }
                        }?: throw NotFoundException("Fant ikke GP for pasient $pasient", uri=cfg.url)
                    } ?: throw NotFoundException("Fant ikke kontraktassosiasjon for pasient $pasient", uri=cfg.url)
                } ?: throw NotFoundException("Fant ikke kontraktassosiasjoner for pasient $pasient", uri=cfg.url)
            } ?: throw NotFoundException("Fant ikke doktorsykler for pasient $pasient", uri=cfg.url)
        } ?: throw NotFoundException("Fant ikke GP detaljer for pasient $pasient", uri=cfg.url)
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege for pasient $pasient",it.message, cfg.url, it)
            else -> throw it
        }
    }

    data class LegeListe(val lege: Person, val pasienter: List<Person>)
    data class Person(val fnr: Fødselsnummer, val navn: Navn)
    data class Lege(val kontraktId: Long, val person: Person)

    fun kontorViaPasient(pasient: String) =
        runCatching {
            with(client.getPatientGPDetails(pasient)) {
                with(gpContract.value) {
                    fastlegeKontor(gpOffice.value, gpOfficeOrganizationNumber)
                }
            }
        }.getOrElse {
            when (it) {
                is ReadFault -> throw NotFoundException("Fant ikke detaljer for pasient $pasient", it.message ,cfg.url,it)
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

    companion object {
        private val OF = ObjectFactory()
    }
}
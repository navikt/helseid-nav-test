package no.nav.helseidnavtest.oppslag.fastlege

import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.error.NotFoundException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nhn.schemas.reg.flr.IFlrReadOperations
import no.nhn.schemas.reg.flr.ObjectFactory
import no.nhn.schemas.reg.flr.WSGPOffice
import org.springframework.stereotype.Component
import no.nhn.schemas.reg.flr.IFlrReadOperationsGetPatientGPDetailsGenericFaultFaultFaultMessage as ReadFault

@Component
class FastlegeCXFAdapter(cfg: FastlegeConfig) : AbstractCXFAdapter(cfg) {

    private val client = client<IFlrReadOperations>()

    fun pasienterForAvtale(avtale: AvtaleId) = runCatching {
        client.getGPPatientList(avtale.verdi)?.patientToGPContractAssociation?.map {
            with(it.patient.value) {
                Person(Fødselsnummer(nin.value), Navn(firstName.value, middleName.value, lastName.value))
            }
        }
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlegeliste for avtale $avtale", cfg.url, cause = it)
            else -> throw it
        }
    }

    fun pasienterForFastlege(navn: String) = runCatching {
        val search = OF.createWSGPSearchParameters().apply {
            fullText = OF.createWSGPContractQueryParametersFullText(navn)
            page = 1
            pageSize = 10
        }
        client.searchForGP(search).results.value.gpDetails.map { d ->

            val lege = with(d.gp.value) {
                Person(
                    Fødselsnummer(nin.value),
                    Navn(firstName.value, middleName.value, lastName.value)
                )
            }
            if (d.contracts.isNil) throw NotFoundException("Fant ikke kontrakter for fastlege $navn", uri = cfg.url)
            if (d.contracts.value.gpOnContractAssociation.isNullOrEmpty()) throw NotFoundException(
                "Fant ikke kontraktassosiasjoner for fastlege $navn",
                uri = cfg.url
            )
            log.info("Vi har kontraktassosiasjoner for fastlege $navn ${d.contracts.value.gpOnContractAssociation.size}")
            val x = d.contracts?.value?.gpOnContractAssociation?.map { a ->
                log.info("KontraktId: ${a.gpContractId}")
                a.gpContract.value.patientList.value?.patientToGPContractAssociation?.map { l ->
                    with(l.patient.value) {
                        Person(Fødselsnummer(nin.value), Navn(firstName.value, middleName.value, lastName.value))
                    }
                } ?: emptyList()
            }?.flatten() ?: emptyList()
            LegeListe(lege, x)
        }
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege med $navn", cfg.url, cause = it)
            else -> throw it
        }
    }

    fun herIdForLegeViaPasient(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient).gpHerId.value
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege for pasient $pasient", cfg.url, cause = it)
            else -> throw it
        }
    }

    fun lege(pasient: String) = runCatching {
        client.getPatientGPDetails(pasient)?.let { d ->
            d.doctorCycles?.value?.let { a ->
                a.gpOnContractAssociation?.firstNotNullOfOrNull { c ->
                    c.gp.value?.let {
                        Lege(
                            d.gpContractId,
                            Person(
                                Fødselsnummer(it.nin.value),
                                Navn(it.firstName.value, it.middleName.value, it.lastName.value)
                            )
                        )
                    }
                } ?: throw NotFoundException("Fant ikke GP for pasient $pasient", uri = cfg.url)
            } ?: throw NotFoundException("Fant ikke kontraktassosiasjoner for pasient $pasient", uri = cfg.url)
        } ?: throw NotFoundException("Fant ikke GP detaljer for pasient $pasient", uri = cfg.url)
    }.getOrElse {
        when (it) {
            is ReadFault -> throw NotFoundException("Fant ikke fastlege for pasient $pasient", cfg.url, cause = it)
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
                is ReadFault -> throw NotFoundException("Fant ikke detaljer for pasient $pasient", cfg.url, cause = it)
                else -> throw it
            }
        }

    private fun fastlegeKontor(kontor: WSGPOffice, orgnr: Int) =
        with(kontor) {
            with(physicalAddresses.value.physicalAddress.first()) {
                BehandlerKontor(
                    name.value, streetAddress.value,
                    Postnummer(postalCode), city.value, Orgnummer(orgnr)
                )
            }
        }

    override fun ping() = emptyMap<String, String>()

    companion object {
        private val OF = ObjectFactory()
    }
}
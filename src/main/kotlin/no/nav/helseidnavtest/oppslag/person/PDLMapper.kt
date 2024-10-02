package no.nav.helseidnavtest.oppslag.person

import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLPersonResponse.PDLPerson
import no.nav.helseidnavtest.oppslag.person.PDLPersonResponse.PDLPerson.PDLBostedadresse.PDLVegadresse
import no.nav.helseidnavtest.oppslag.person.PDLPersonResponse.PDLPerson.PDLFødsel
import no.nav.helseidnavtest.oppslag.person.PDLPersonResponse.PDLPerson.PDLNavn
import no.nav.helseidnavtest.oppslag.person.Person.Adresse
import no.nav.helseidnavtest.oppslag.person.Person.Adresse.PostNummer
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import java.time.LocalDate

object PDLMapper {

    private val log = getLogger(javaClass)

    fun pdlPersonTilPerson(person: PDLPerson?, fnr: Fødselsnummer) =
        person?.let {
            with(it) {
                Person(
                    navnFra(navn), fnr,
                    adresseFra(vegadresse),
                    fødselsdatoFra(fødsel)
                ).also {
                    log.trace(CONFIDENTIAL, "Person er {}", it)
                }
            }
        }

    private fun navnFra(navn: PDLNavn) =
        with(navn) {
            Person.Navn(fornavn, mellomnavn, etternavn).also {
                log.trace(CONFIDENTIAL, "Navn er {}", it)
            }
        }

    private fun adresseFra(adresse: PDLVegadresse?) =
        adresse?.let {
            with(it) {
                Adresse(adressenavn, husbokstav, husnummer, PostNummer(postnummer))
            }
        }

    private fun fødselsdatoFra(fødsel: PDLFødsel?) =
        fødsel?.fødselsdato

}

data class Person(
    val navn: Navn,
    val fnr: Fødselsnummer,
    val adresse: Adresse? = null,
    val fødseldato: LocalDate? = null
) {
    data class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String) {
        val visningsNavn = listOf(fornavn, mellomnavn, etternavn).joinToString(separator = " ")
    }

    data class Adresse(
        val adressenavn: String?,
        val husbokstav: String?,
        val husnummer: String?,
        val postnummer: PostNummer?
    ) {
        data class PostNummer(val postnr: String?, val poststed: String?) {
            constructor(postnr: String?) : this(postnr, poststeder[postnr] ?: "Ukjent poststed for $postnr")

            companion object {
                private val poststeder = try {
                    ClassPathResource("postnr.txt").inputStream.bufferedReader()
                        .lines()
                        .map { it.split("\\s+".toRegex()) }
                        .map { it[0] to it[1] }
                        .toList()
                        .associate { it.first to it.second }
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        }
    }
}


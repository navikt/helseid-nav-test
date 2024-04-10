package no.nav.helseidnavtest.oppslag.person

import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import java.time.LocalDate

object PDLMapper {

    private val log = getLogger(javaClass)

    fun pdlSøkerTilSøker(søker : PDLSøker?, fnr : Fødselsnummer) = søker?.let {
        with(it) {
            Søker(navnFra(navn), fnr,
                adresseFra(vegadresse),
                fødselsdatoFra(fødsel)).also {
                log.trace(CONFIDENTIAL, "Søker er {}", it)
            }
        }
    }


    private fun navnFra(navn : Set<PDLNavn>) = navnFra(navn.first())


    private fun navnFra(navn : PDLNavn) =
        with(navn) {
            Navn(fornavn, mellomnavn, etternavn).also {
                log.trace(CONFIDENTIAL, "Navn er {}", it)
            }
        }

    private fun adresseFra(adresse : PDLSøker.PDLBostedadresse.PDLVegadresse?) = adresse?.let {
        with(it) {
            Adresse(adressenavn, husbokstav, husnummer, PostNummer(postnummer))
        }
    }
    private fun fødselsdatoFra(fødsel : PDLSøker.PDLFødsel?) = fødsel?.fødselsdato

}

data class Søker(val navn: Navn, val fnr: Fødselsnummer, val adresse: Adresse? = null, val fødseldato: LocalDate? = null)

data class Navn(val fornavn : String?, val mellomnavn : String?, val etternavn : String?)
data class Adresse(val adressenavn : String?, val husbokstav : String?, val husnummer : String?, val postnummer : PostNummer?)

data class PostNummer(val postnr : String?, val poststed : String?) {
    constructor(postnr : String?) : this(postnr, poststeder[postnr] ?: "Ukjent poststed for $postnr")

    companion object {

        private val poststeder = try {
            ClassPathResource("postnr.txt").inputStream.bufferedReader()
                .lines()
                .map { it.split("\\s+".toRegex()) }
                .map { it[0] to it[1] }
                .toList()
                .associate { it.first to it.second }
        }
        catch (e : Exception) {
            emptyMap()
        }
    }
}


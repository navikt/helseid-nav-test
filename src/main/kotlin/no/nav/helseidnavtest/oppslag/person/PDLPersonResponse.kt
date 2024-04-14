package no.nav.helseidnavtest.oppslag.person

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helseidnavtest.oppslag.person.PDLPerson.*
import no.nav.helseidnavtest.oppslag.person.PDLPerson.PDLBostedadresse.*
import java.time.LocalDate


data class PDLWrappedPerson(val navn: Set<PDLNavn>,
                            @JsonProperty("foedsel") val fødsel: Set<PDLFødsel>,
                            val bostedsadresse: List<PDLBostedadresse> = emptyList()) {
    val active = PDLPerson(navn.first(), fødsel.firstOrNull(), bostedsadresse.firstOrNull()?.vegadresse)
}

data class PDLNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

data class PDLPerson(val navn: PDLNavn,
                     val fødsel: PDLFødsel?,
                     val vegadresse: PDLVegadresse?) {


    data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate?)

    data class PDLBostedadresse(val vegadresse: PDLVegadresse?) {
        data class PDLVegadresse(val adressenavn: String,
                                 val husbokstav: String?,
                                 val husnummer: String?,
                                 val postnummer: String)
    }
}


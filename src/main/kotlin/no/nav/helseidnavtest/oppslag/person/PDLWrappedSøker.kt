package no.nav.helseidnavtest.oppslag.person

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate


data class PDLWrappedSøker(val navn: Set<PDLNavn>,
                           @JsonProperty("foedsel") val fødsel: Set<PDLSøker.PDLFødsel>,
                           val bostedsadresse: List<PDLSøker.PDLBostedadresse> = emptyList()) {
    val active = PDLSøker(navn.first(), fødsel.firstOrNull(), bostedsadresse.firstOrNull()?.vegadresse)
}

enum class PDLAdresseBeskyttelse {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}
data class PDLGradering(val gradering: PDLAdresseBeskyttelse)
data class PDLNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

data class PDLSøker(val navn: PDLNavn,
                    val fødsel: PDLFødsel?,
                    val vegadresse: PDLBostedadresse.PDLVegadresse?) {


    data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate?)

    data class PDLBostedadresse(val vegadresse: PDLVegadresse?) {
        data class PDLVegadresse(val adressenavn: String,
                                 val husbokstav: String?,
                                 val husnummer: String?,
                                 val postnummer: String)
    }
}


package no.nav.helseidnavtest.oppslag.arbeid

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helseidnavtest.oppslag.organisasjon.OrgNummer
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArbeidsforholdDTO(val ansettelsesperiode: AnsettelsesperiodeDTO,
                             val arbeidsavtaler: List<ArbeidsavtaleDTO>,
                             val arbeidsgiver: ArbeidsgiverDTO) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AnsettelsesperiodeDTO(val periode: Periode)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ArbeidsavtaleDTO(val stillingsprosent: Double, val antallTimerPrUke: Double) {
        fun tilAvtale(p: Periode) = Arbeidsforhold.Arbeidsavtale(stillingsprosent, antallTimerPrUke, p)
    }

    data class ArbeidsgiverDTO(val type: ArbeidsgiverType, val organisasjonsnummer:
    OrgNummer) {
        enum class ArbeidsgiverType {
            Organisasjon,
            Person
        }
    }

    fun tilArbeidInfo(orgNavn: String) =
        Arbeidsforhold(orgNavn, arbeidsavtaler.map {
            it.tilAvtale(ansettelsesperiode.periode)
        })
}

data class Arbeidsforhold(val navn: String, val avtaler: List<Arbeidsavtale>) {
    data class Arbeidsavtale(val stillingsprosent: Double, val antallTimerPrUke: Double, val periode: Periode)
}

data class Periode(val fom: LocalDate, val tom: LocalDate?)
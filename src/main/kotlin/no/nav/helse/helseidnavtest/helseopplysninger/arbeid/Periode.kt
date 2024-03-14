package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Duration
import java.time.LocalDate

data class Periode(val fom : LocalDate, val tom : LocalDate?) {

    @JsonIgnore
    val varighetDager = tom?.let { Duration.between(fom.atStartOfDay(), it.atStartOfDay()).toDays() } ?: -1
}

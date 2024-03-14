package no.nav.helse.helseidnavtest.helseopplysninger.arbeid

import java.net.URI
import java.time.LocalDate.now
import java.time.Period
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.PeriodFormat
import org.springframework.boot.convert.PeriodStyle.*
import org.springframework.web.util.UriBuilder
import no.nav.helse.helseidnavtest.helseopplysninger.arbeid.ArbeidConfig.Companion.ARBEID

@ConfigurationProperties(ARBEID)
class ArbeidConfig(baseUri: URI, private val path: String = PATH, enabled: Boolean = true,
                   pingPath: String = PINGPATH,
                   @PeriodFormat(SIMPLE) private val tidTilbake: Period = Period.ofYears(5),
                   val sporingsinformasjon: Boolean = false) :
    AbstractRestConfig(baseUri, pingPath, ARBEID, enabled) {

    fun arbeidsforholdURI(b: UriBuilder) =
        b.path(path)
            .queryParam(HISTORIKK, false)
            .queryParam(SPORINGSINFORMASJON, sporingsinformasjon)
            .queryParam(FOM, now().minus(tidTilbake).format(ISO_LOCAL_DATE))
            .build()

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  path=$path, pingEndpoint=$pingEndpoint, tidTilbake=$tidTilbake]"

    companion object {
        const val ARBEID = "arbeidsforhold"
        private  const val PINGPATH = "internal/isAlive"
        private const val PATH = "api/v1/arbeidstaker/arbeidsforhold"
        private const val FOM = "ansettelsesperiodeFom"
        private const val SPORINGSINFORMASJON = "sporingsinformasjon"
        private const val HISTORIKK = "historikk"
    }
}
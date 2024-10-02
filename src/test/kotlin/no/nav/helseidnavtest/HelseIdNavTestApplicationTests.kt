package no.nav.helseidnavtest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_1
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.NAV
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Tjeneste
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.Virksomhet
import org.junit.jupiter.api.Test

//@SpringBootTest
class HelseIdNavTestApplicationTests {

    @Test
    fun contextLoads() {
        val m1 = jacksonObjectMapper()
        val m = m1.writerWithDefaultPrettyPrinter()
        val t = Tjeneste(EDI_1.first,
            "navn",
            Virksomhet(NAV, "NAV", true),
            true)
        val s = m.writeValueAsString(t)
        val t1 = m1.readValue<Tjeneste>(s)
        println(t1)
    }

}
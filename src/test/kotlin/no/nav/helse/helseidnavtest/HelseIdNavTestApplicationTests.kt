package no.nav.helse.helseidnavtest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helseidnavtest.edi20.Apprec
import org.junit.jupiter.api.Test

//@SpringBootTest
class HelseIdNavTestApplicationTests {

    @Test
    fun contextLoads() {
        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Apprec.OK))
    }

}
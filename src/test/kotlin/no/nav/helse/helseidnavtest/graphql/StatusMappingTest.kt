package no.nav.helse.helseidnavtest.graphql

import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.*
import java.net.URI

class StatusMappingTest  {

    @Test
    fun mapStatus() {
        val e = GraphQLErrorHandler.oversett("unauthorized", "Feil", URI.create("http://localhost:8080"))
        assertThat(e.statusCode).isEqualTo(UNAUTHORIZED)
    }
}
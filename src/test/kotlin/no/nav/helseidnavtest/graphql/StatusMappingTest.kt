package no.nav.helseidnavtest.graphql

import no.nav.helseidnavtest.oppslag.graphql.GraphQLErrorHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.UNAUTHORIZED
import java.net.URI

class StatusMappingTest {

    @Test
    fun mapStatus() {
        val e = GraphQLErrorHandler.oversett("unauthorized", "Feil", URI.create("http://localhost:8080"))
        assertThat(e.statusCode).isEqualTo(UNAUTHORIZED)
    }
}
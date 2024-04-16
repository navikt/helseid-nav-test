package no.nav.helseidnavtest.dialogmelding
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import no.nav.helseidnavtest.oppslag.person.Person
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity.status
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/xml")
class DialogmeldingController(private val generator: DialogmeldingGenerator) {

    @GetMapping(value = ["/melding"])
    fun xml(@RequestParam pasient: Fødselsnummer) = generator.genererDialogmelding(pasient)
}
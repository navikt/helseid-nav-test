package no.nav.helseidnavtest.dialogmelding
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
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
class DialogmeldingController(private val pdl: PDLClient) {

    private val log = getLogger(DialogmeldingController::class.java)

    @GetMapping(value = ["/melding"])
    fun xml(@RequestParam pasient: Fødselsnummer) : String? {
        val a = SecurityContextHolder.getContext().authentication //as OAuth2AuthenticationToken
       // a.principal.name.also { log.info("principal name is {}", it) }
        log.info("auth is {}", a)
        a.name.also { log.info("Token name  is {}", it)
        val navn = pdl.navn(pasient).also { log.info("Navn er {}", this) }

    val kontor = BehandlerKontor(
        partnerId = PartnerId(123456789),
        navn = "Et legekontor",
        orgnummer = Virksomhetsnummer("123456789"),
        postnummer = "1234",
        poststed = "Oslo",
        adresse = "Fyrstikkalleen 1",
        herId = 12345678)

    val behandler =  Behandler(
        UUID.randomUUID(),
        fornavn = "Ole",
        mellomnavn ="Mellomnavn",
        etternavn = "Olsen",
        herId = 123456789,
        hprId = 987654321,
        telefon = "12345678",
        personident = Personident("12345678901"),
        kontor = kontor)

    val b = DialogmeldingBestilling(uuid = UUID.randomUUID(),
        behandler = behandler,
        arbeidstakerPersonident =  Personident("01010111111"),
        parentRef = "parent ref",
        conversationUuid =  UUID.randomUUID(),
        tekst = "dette er litt tekst",
        vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
    )
    val arbeidstaker = Arbeidstaker(
        arbeidstakerPersonident = Personident(pasient.fnr),
        fornavn = navn.fornavn!!,
        mellomnavn = navn.mellomnavn,
        etternavn = navn.etternavn!!)
    val m  = DialogmeldingMapper.opprettDialogmelding(b, arbeidstaker)
        return m.message.also { log.trace("XML {}", this) }
        }
    }
}
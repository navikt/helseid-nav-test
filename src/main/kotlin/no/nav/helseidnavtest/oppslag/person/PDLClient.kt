package no.nav.helseidnavtest.oppslag.person
import no.nav.helseidnavtest.dialogmelding.DialogmeldingController
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLConfig.Companion.PDL
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Component
class PDLClient(private val pdl: PDLWebClientAdapter) {

    private val log = getLogger(PDLClient::class.java)

    fun ping() = pdl.ping()
    @Retryable(retryFor = [RecoverableException::class])
    fun navn(fnr: Fødselsnummer) = pdl.person(fnr).navn
    @Recover
    fun recover(ex: RecoverableException, fnr: Fødselsnummer) :Nothing= throw ex.also {
        log.error("Recoverable exception feilet for fnr {}", fnr)
    }
}
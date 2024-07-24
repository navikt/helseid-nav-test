package no.nav.helseidnavtest.oppslag.person

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component


@Component
@Retryable(retryFor = [RecoverableException::class])
class PDLClient(private val pdl: PDLRestClientAdapter) {

    private val log = getLogger(PDLClient::class.java)

    fun ping() = pdl.ping()
    fun navn(fnr: Fødselsnummer) = pdl.person(fnr).navn

    @Recover
    fun navn(ex: Exception, fnr: Fødselsnummer): Navn = throw ex.also {
        log.error("Recoverable exception feilet for oppslag på fnr {}", fnr, it)
    }
}
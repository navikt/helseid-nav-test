package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Orgnummer
import no.nav.helseidnavtest.error.RecoverableException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
@Retryable(include = [RecoverableException::class])
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    private val log = getLogger(AdresseRegisterClient::class.java)

    fun herIdForOrgnummer(nummer: Orgnummer) = HerId(adapter.herIdForId(nummer.verdi))

    fun navn(id: HerId) = adapter.nameForId(id.verdi)

    @Recover
    fun herIdForOrgnummer(e: Exception, nummer: Orgnummer): HerId = throw e.also {
        log.error("Recoverable exception feilet for oppslag på orgnummer {}", nummer, it)
    }
}
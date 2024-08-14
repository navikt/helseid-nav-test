package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.Orgnummer
import no.nav.helseidnavtest.edi20.EDI20DialogmeldingGenerator.*
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.*
import org.slf4j.LoggerFactory.getLogger
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
@Retryable(include = [RecoverableException::class])
class AdresseRegisterClient(private val adapter: AdresseRegisterCXFAdapter) {

    private val log = getLogger(AdresseRegisterClient::class.java)

    fun herIdForOrgnummer(nummer: Orgnummer) = HerId(adapter.herIdForId(nummer.verdi))

    fun kommunikasjonsParter(fra: HerId, til: HerId) =
        KommunikasjonsParter(kommunikasjonsPart(fra), kommunikasjonsPart(til))

    fun kommunikasjonsPart(herId: HerId) = adapter.getParty(herId.verdi)

    @Throws(Exception::class)
    @Recover
    fun herIdForOrgnummer(e: Exception, nummer: Orgnummer): HerId = throw e.also {
        log.error("Recoverable exception feilet for oppslag på orgnummer {}", nummer, it)
    }

    @Recover
    fun partInfo(e: Exception, id: HerId): Pair<String, String> = throw e.also {
        log.error("Recoverable exception feilet for navn oppslag på herid {}", id, it)
    }
}
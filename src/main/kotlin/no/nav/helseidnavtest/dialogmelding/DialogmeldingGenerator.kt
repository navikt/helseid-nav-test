package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.opprettDialogmelding
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.oidcUser
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus.*
import org.springframework.retry.annotation.Retryable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import java.util.*

@Component
class DialogmeldingGenerator(private val pdl: PDLClient) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) =
        when (val a = SecurityContextHolder.getContext().authentication) {
            is OidcUser -> {
                val extractor = ClaimsExtractor(a.claims)
                a.claims.forEach { (k, v) -> log.info("key: $k, value: $v") }
                val behandler = behandler(extractor.navn,extractor.hprNumber, extractor.fnr, behandlerKontor())
                val bestilling = bestilling(behandler)
                val arbeidstaker = arbeidstaker(pasient)
                opprettDialogmelding(bestilling, arbeidstaker).message
            }
            else -> {
                log.error("Uventet token type {}, refresh Swagger om det er der du ser dette", a::class.java)
                throw IrrecoverableException(INTERNAL_SERVER_ERROR,"Uventet type", "${a::class.java}")
            }
        }


    private fun arbeidstaker(pasient: Fødselsnummer) =
        with(pdl.navn(pasient)) {
            Arbeidstaker(Personident(pasient.fnr), fornavn, mellomnavn, etternavn)
        }

    private fun bestilling(behandler: Behandler) =
        DialogmeldingBestilling(
            UUID.randomUUID(),
            behandler,
            Personident("01010111111"),
            "parent ref",
            UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
        )

    private fun behandler(navn: Navn, hpr: Int, fnr: Fødselsnummer, kontor: BehandlerKontor) =
        Behandler(
            UUID.randomUUID(),
            fornavn = navn.fornavn,
            mellomnavn = navn.mellomnavn,
            etternavn = navn.etternavn,
            herId = 42,
            hprId = hpr,
            telefon = "12345678",
            personident = Personident(fnr.fnr),
            kontor = kontor
        )

    private fun behandlerKontor() = BehandlerKontor(
        partnerId = PartnerId(123456789),
        navn = "Et legekontor",
        orgnummer = Virksomhetsnummer("123456789"),
        postnummer = "1234",
        poststed = "Oslo",
        adresse = "Fyrstikkalleen 1",
        herId = 12345678
    )
}

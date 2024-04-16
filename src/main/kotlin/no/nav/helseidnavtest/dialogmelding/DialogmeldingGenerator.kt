package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.xmlFra
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nav.helseidnavtest.security.ClaimsExtractor
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus.*
import org.springframework.retry.annotation.Retryable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import java.util.*

@Component
class DialogmeldingGenerator(private val pdl: PDLClient) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) =
        when (val a = SecurityContextHolder.getContext().authentication) {
            is OAuth2AuthenticationToken -> dialogmelding(ClaimsExtractor(a.principal.attributes), pasient)
            else -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,"Uventet type", "${a::class.java}").also {
                log.error("Uventet token type {}, refresh Swagger om det er der du ser dette", a::class.java)
            }
        }

    private fun dialogmelding(extractor: ClaimsExtractor, pasient: Fødselsnummer) =
        xmlFra(dialogmelding(extractor.let {
            behandler(it.navn, it.hprNumber, it.fnr, behandlerKontor())
        }), arbeidstaker(pasient)).message

    private fun arbeidstaker(pasient: Fødselsnummer) =
        with(pdl.navn(pasient)) {
            Arbeidstaker(Personident(pasient.fnr), fornavn, mellomnavn, etternavn)
        }

    private fun dialogmelding(behandler: Behandler) =
        Dialogmelding(
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

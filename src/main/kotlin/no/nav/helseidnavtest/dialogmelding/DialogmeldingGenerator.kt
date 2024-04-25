package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.xmlFra
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.fastlege.FastlegeClient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nav.helseidnavtest.security.ClaimsExtractor
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus.*
import org.springframework.retry.annotation.Retryable
import org.springframework.security.core.context.SecurityContextHolder.*
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID.*

@Component
class DialogmeldingGenerator(private val pdl: PDLClient, private val fastlege: FastlegeClient) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) =
        when (val auth = getContext().authentication) {
            is OAuth2AuthenticationToken -> dialogmelding(ClaimsExtractor(auth.principal.attributes), pasient)
            else -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,"Uventet type", "${auth::class.java}").also {
                log.error("Uventet token type {}, refresh Swagger om det er der du ser dette", auth::class.java)
            }
        }

    private fun dialogmelding(extractor: ClaimsExtractor, pasient: Fødselsnummer) =
        xmlFra(dialogmelding(with(extractor)  {
            behandler(navn, 42, hprNumber, fnr, kontor(pasient))
        }), arbeidstaker(pasient)).message

    private fun arbeidstaker(pasient: Fødselsnummer) =
        with(pdl.navn(pasient)) {
            Arbeidstaker(pasient, fornavn, mellomnavn, etternavn)
        }

    private fun dialogmelding(behandler: Behandler) =
        Dialogmelding(randomUUID(), behandler, Fødselsnummer("26900799232"), "parent ref",
            randomUUID(), "dette er litt tekst", ClassPathResource("test.pdf").inputStream.readBytes(),
        )

    private fun behandler(navn: Navn, herID: Int,hpr: Int, fnr: Fødselsnummer, kontor: BehandlerKontor) =
        Behandler(randomUUID(), fnr,
            navn.fornavn, navn.mellomnavn, navn.etternavn,
            herID, hpr, "12345678", kontor)

    private fun kontor(pasient: Fødselsnummer) = fastlege.kontor(pasient)
}

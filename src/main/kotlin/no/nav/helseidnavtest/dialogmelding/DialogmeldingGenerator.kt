package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
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
class DialogmeldingGenerator(private val mapper: DialogmeldingMapper,private val pdl: PDLClient, private val fastlege: FastlegeClient, private val adresseAdapter: AdresseWSAdapter) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) =
        when (val auth = getContext().authentication) {
            is OAuth2AuthenticationToken -> {
                mapper.xmlFra(dialogmelding(with(ClaimsExtractor(auth.principal.attributes)) {
                    behandler(navn, fastlege.herId(pasient), HprId(hprNumber), fnr, fastlege.kontor(pasient))
            }), arbeidstaker(pasient))
        }
            else -> throw IrrecoverableException(INTERNAL_SERVER_ERROR, "Uventet type", "${auth::class.java}").also {
                log.error("Uventet token type {}, refresh Swagger om det er der du ser dette", auth::class.java)
            }
        }

    private fun arbeidstaker(pasient: Fødselsnummer) =
        with(pdl.navn(pasient)) {
            Arbeidstaker(pasient, fornavn, mellomnavn, etternavn)
        }

    private fun dialogmelding(behandler: Behandler) =
        Dialogmelding(randomUUID(), behandler, Fødselsnummer("26900799232"),
            randomUUID(), "dette er litt tekst", ClassPathResource("test.pdf").inputStream.readBytes(),
        )

    private fun behandler(navn: Navn, herId: HerId,hprId: HprId, fnr: Fødselsnummer, kontor: BehandlerKontor) =
        with(navn) {
            Behandler(randomUUID(), fnr,
                fornavn, mellomnavn, etternavn,
                herId, hprId, "12345678", kontor) // TODO
        }
}

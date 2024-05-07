package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterWSAdapter
import no.nav.helseidnavtest.oppslag.fastlege.FastlegeClient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.*
import no.nav.helseidnavtest.security.ClaimsExtractor
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus.*
import org.springframework.retry.annotation.Retryable
import org.springframework.security.core.context.SecurityContextHolder.*
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.UUID.*

@Component
class DialogmeldingGenerator(private val mapper: DialogmeldingMapper,private val pdl: PDLClient, private val fastlege: FastlegeClient, private val adresseAdapter: AdresseRegisterWSAdapter) {


    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer, uuid: UUID) =
        when (val auth = getContext().authentication) {
            is OAuth2AuthenticationToken -> {
                mapper.xmlFra(dialogmelding(with(ClaimsExtractor(auth.principal.attributes)) {
                    behandler(navn, fastlege.herId(pasient), HprId(hprNumber), fnr, fastlege.kontor(pasient))
            },uuid), arbeidstaker(pasient))
        }
            else -> throw IrrecoverableException(FORBIDDEN, "Ikke autentisert", "${auth::class.java}")
        }

    private fun arbeidstaker(pasient: Fødselsnummer) = Arbeidstaker(pasient, pdl.navn(pasient))


    private fun dialogmelding(behandler: Behandler, uuid: UUID) =
        Dialogmelding(uuid, behandler, Fødselsnummer("26900799232"),
            uuid, "dette er litt tekst", ClassPathResource("test.pdf").inputStream.readBytes(),
        )

    private fun behandler(navn: Navn, herId: HerId,hprId: HprId, fnr: Fødselsnummer, kontor: BehandlerKontor) =
        with(navn) {
            Behandler(randomUUID(), fnr,
                Navn(fornavn, mellomnavn, etternavn),
                herId, hprId, "12345678", kontor) // TODO
        }
}

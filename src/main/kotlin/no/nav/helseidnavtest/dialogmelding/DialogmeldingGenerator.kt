package no.nav.helseidnavtest.dialogmelding

import no.nav.helseidnavtest.dialogmelding.DialogmeldingMapper.opprettDialogmelding
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseidnavtest.oppslag.person.PDLClient
import no.nav.helseidnavtest.oppslag.person.Person.*
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus.*
import org.springframework.retry.annotation.Retryable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import java.util.*

@Component
class DialogmeldingGenerator(private val pdl: PDLClient) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) =
        when (val a = SecurityContextHolder.getContext().authentication) {
            is OAuth2AuthenticationToken -> {
                log.info(a.toString())
                //a.principal.attributes.forEach { (k, v) -> log.info("key: $k, value: $v") }
                val behandler = behandler(a.navn(),a.hpr(), behandlerKontor())
                val bestilling = bestilling(behandler)
                val arbeidstaker = arbeidstaker(pasient)
                opprettDialogmelding(bestilling, arbeidstaker).message.also {
                    log.trace("XML {}", this)
                }
            }
            else -> {
                log.error("Uventet type {}", a::class.java)
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

    private fun behandler(
        behandlerNavn: Navn,
        hpr: Int,
        kontor: BehandlerKontor
    ) = Behandler(
        UUID.randomUUID(),
        fornavn = behandlerNavn.fornavn,
        mellomnavn = behandlerNavn.mellomnavn,
        etternavn = behandlerNavn.etternavn,
        herId = 42,
        hprId = hpr,
        telefon = "12345678",
        personident = Personident("12345678901"),
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
private fun OAuth2AuthenticationToken.attribute(a: String) = principal.attribute(a)

private fun OAuth2AuthenticationToken.hpr() = attribute("helseid://claims/hpr/hpr_number").toInt()


private fun OAuth2AuthenticationToken.navn() =
    with(principal)  {
        Navn(attribute("given_name"), attribute("middle_name"), attribute("family_name"))
    }

private fun OAuth2User.attribute(a: String) =
   getAttribute<String>(a) ?: throw IrrecoverableException(INTERNAL_SERVER_ERROR, "Mangler attributt", a)

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
import org.springframework.stereotype.Component
import java.util.*

@Component
class DialogmeldingGenerator(private val pdl: PDLClient) {

    private val log = getLogger(DialogmeldingGenerator::class.java)

    @Retryable(retryFor = [RecoverableException::class])
    fun genererDialogmelding(pasient: Fødselsnummer) : String {
        when (val a = SecurityContextHolder.getContext().authentication) {
        is OAuth2AuthenticationToken -> {
            log.info(a.toString())
            //a.principal.attributes.forEach { (k, v) -> log.info("key: $k, value: $v") }
            val kontor = BehandlerKontor(
                    partnerId = PartnerId(123456789),
                    navn = "Et legekontor",
                    orgnummer = Virksomhetsnummer("123456789"),
                    postnummer = "1234",
                    poststed = "Oslo",
                    adresse = "Fyrstikkalleen 1",
                    herId = 12345678)

            val behandlerNavn = a.navn()
            val behandler =  Behandler(
                    UUID.randomUUID(),
                    fornavn = behandlerNavn.fornavn,
                    mellomnavn =behandlerNavn.mellomnavn,
                    etternavn = behandlerNavn.etternavn,
                    herId = 123456789,
                    hprId = 987654321,
                    telefon = "12345678",
                    personident = Personident("12345678901"),
                    kontor = kontor)

            val bestilling = DialogmeldingBestilling(uuid = UUID.randomUUID(),
                    behandler = behandler,
                    arbeidstakerPersonident =  Personident("01010111111"),
                    parentRef = "parent ref",
                    conversationUuid =  UUID.randomUUID(),
                    tekst = "dette er litt tekst",
                    vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
                )
            with(pdl.navn(pasient)) {
                val arbeidstaker = Arbeidstaker(Personident(pasient.fnr), fornavn, mellomnavn, etternavn)
                return opprettDialogmelding(bestilling, arbeidstaker).message.also {
                    log.trace("XML {}", this)
                }
            }
        }
        else -> {
            log.error("Uventet type {}", a::class.java)
            throw IrrecoverableException(INTERNAL_SERVER_ERROR,"Uventet type", "${a::class.java}")
        }
    }
    }
}

private fun OAuth2AuthenticationToken.navn() =
    with(principal)  {
        Navn(getAttribute("given_name")!!, getAttribute("middle_name"), getAttribute("family_name")!!)
    }
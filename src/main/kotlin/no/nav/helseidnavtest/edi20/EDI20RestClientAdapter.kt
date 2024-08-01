package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.error.BodyConsumingErrorHandler
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.Base64.getEncoder

@Component
class EDI20RestClientAdapter(
    @Qualifier(EDI20) restClient: RestClient,
    private val cf: EDI20Config,
    private val generator: EDI20DialogmeldingGenerator,
    @Qualifier(EDI20) private val handler: BodyConsumingErrorHandler
) : AbstractRestClientAdapter(restClient, cf) {

    fun apprec(herId: HerId, id: UUID) =
        restClient
            .post()
            .uri { cf.apprecURI(it, id, herId.verdi) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .body(Apprec.OK)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<String>()

    fun status(herId: HerId, id: UUID) =
        restClient
            .get()
            .uri { cf.statusURI(it, id) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<List<Status>>()

    fun les(herId: HerId, id: UUID) =
        restClient
            .get()
            .uri { cf.lesURI(it, id) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<String>()

    fun poll(herId: HerId, appRec: Boolean) =
        restClient
            .get()
            .uri { cf.pollURI(it, herId.verdi, appRec) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .body<List<Meldinger>>()

    fun send(herId: HerId, pasient: Fødselsnummer, vedlegg: MultipartFile?) =
        restClient
            .post()
            .uri(cf::sendURI)
            .headers { it.herId(herId.verdi) }
            .accept(APPLICATION_JSON)
            .body(BusinessDocument(xml(herId, herId.other(), pasient).encode()))
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()

    fun lest(herId: HerId, id: UUID) =
        restClient
            .put()
            .uri { cf.lestURI(it, id, herId.verdi) }
            .headers { it.herId(herId) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus({ it.isError }) { req, res -> handler.handle(req, res) }
            .toBodilessEntity()

    private fun xml(from: HerId, to: HerId, pasient: Fødselsnummer) =
        generator.hodemelding(from, to, pasient).also { log.info("XML er $it") }

    private fun String.encode() = getEncoder().withoutPadding().encodeToString(toByteArray())

    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

    companion object {
        val XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns4:MsgHead
	xmlns="http://www.kith.no/xmlstds/base64container"
	xmlns:ns5="http://www.w3.org/2000/09/xmldsig#"
	xmlns:ns2="http://www.kith.no/xmlstds/dialog/2006-10-11"
	xmlns:ns4="http://www.kith.no/xmlstds/msghead/2006-05-24"
	xmlns:ns3="http://www.kith.no/xmlstds/felleskomponent1">
	<ns4:MsgInfo>
		<ns4:Type V="DIALOG_NOTAT" DN="Notat"/>
		<ns4:MIGversion>v1.2 2006-05-24</ns4:MIGversion>
		<ns4:GenDate>2024-07-04T16:55:43.904207659</ns4:GenDate>
		<ns4:MsgId>ce3de801-9cae-4cb6-aa4e-b682cb6de3c3</ns4:MsgId>
		<ns4:Sender>
			<ns4:Organisation>
				<ns4:OrganisationName>ARBEIDS- OG VELFERDSETATEN</ns4:OrganisationName>
				<ns4:Ident>
					<ns4:Id>90128</ns4:Id>
					<ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="HER-id"/>
				</ns4:Ident>
				<ns4:Organisation>
					<ns4:OrganisationName>Samhandling Arbeids- og velferdsetaten</ns4:OrganisationName>
					<ns4:Ident>
						<ns4:Id>%s</ns4:Id>
						<ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="HER-id"/>
					</ns4:Ident>
				</ns4:Organisation>
			</ns4:Organisation>
		</ns4:Sender>
		<ns4:Receiver>
			<ns4:Organisation>
				<ns4:OrganisationName>ARBEIDS- OG VELFERDSETATEN</ns4:OrganisationName>
				<ns4:Ident>
					<ns4:Id>90128</ns4:Id>
					<ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="HER-id"/>
				</ns4:Ident>
				<ns4:Organisation>
					<ns4:OrganisationName>Samhandling Arbeids- og velferdsetaten</ns4:OrganisationName>
					<ns4:Ident>
						<ns4:Id>%s</ns4:Id>
						<ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="HER-id"/>
					</ns4:Ident>
				</ns4:Organisation>
			</ns4:Organisation>
		</ns4:Receiver>
		<ns4:Patient>
			<ns4:FamilyName>KONTROLL</ns4:FamilyName>
			<ns4:GivenName>FIRKANTET</ns4:GivenName>
			<ns4:Ident>
				<ns4:Id>26900799232</ns4:Id>
				<ns4:TypeId V="FNR" S="2.16.578.1.12.4.1.1.8116" DN="Fødselsnummer"/>
			</ns4:Ident>
		</ns4:Patient>
	</ns4:MsgInfo>
</ns4:MsgHead>
    """.trimIndent()
    }
}
package no.nav.helseidnavtest.edi20

import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.DialogmeldingGenerator
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.HERID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.io.StringWriter
import java.lang.String.format
import java.util.*
import java.util.Base64.getEncoder
import kotlin.text.*

@Component
class EDI20RestClientAdapter(@Qualifier(EDI20) restClient: RestClient, private val cf: EDI20Config, private val generator: DialogmeldingGenerator) : AbstractRestClientAdapter(restClient,cf) {

    fun status(id: UUID, herId: HerId) =
        restClient
            .get()
            .uri { b -> cf.statusURI(b,id) }
            .headers { it.add(HERID, herId.verdi) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .body<List<Status>>()

    fun hent(id: UUID, herId: HerId) =
        restClient
            .get()
            .uri { b -> cf.messagesURI(b,id) }
            .headers { it.add(HERID, herId.verdi) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .body<String>()

    fun poll(herId: HerId, appRec:  Boolean) =
        restClient
            .get()
            .uri { b -> cf.messagesURI(b,herId, appRec) }
            .headers { it.add(HERID, herId.verdi) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .body<List<Messages>>()


    fun send(herId: HerId): Any {
        val dok = BusinessDocument(format(XML, herId.verdi, other(herId)).encode())
        return restClient
            .post()
            .uri(cf::messagesPostURI)
            .headers { it.add(HERID, herId.verdi) }
            .accept(APPLICATION_JSON)
            .body(dok)
            .retrieve()
            .toBodilessEntity()
    }

    fun markRead(id:UUID, herId: HerId) =
        restClient
            .put()
            .uri { b -> cf.markReadURI(b,id,herId) }
            .headers { it.add(HERID, herId.verdi) }
            .accept(APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity()

    private fun other(herId: HerId) =
         when(herId.verdi) {
            EDI1_ID ->  EDI2_ID
            EDI2_ID ->  EDI1_ID
            else -> throw IllegalArgumentException("Ikke støttet herId $herId")
    }

    private fun String.encode() = getEncoder().withoutPadding().encodeToString(toByteArray())
    private fun marshal() : String {
        val xml = StringWriter()
        Jaxb2Marshaller().apply {
            setMarshallerProperties(mapOf(JAXB_FORMATTED_OUTPUT to true))
            setClassesToBeBound(
                XMLBase64Container::class.java,
                XMLDialogmelding::class.java,
                XMLMsgHead::class.java)
        }.createMarshaller().marshal(generator.hodemeldng(Fødselsnummer("12345678901"), UUID.randomUUID()), xml)
        log.info("XML {}", xml.toString())
        return xml.toString()
    }


    override fun toString() =
        "${javaClass.simpleName} [restClient=$restClient, cfg=$cfg]"

    companion object {
        val XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <ns4:MsgHead xmlns="http://www.kith.no/xmlstds/base64container" xmlns:ns5="http://www.w3.org/2000/09/xmldsig#" xmlns:ns2="http://www.kith.no/xmlstds/dialog/2006-10-11" xmlns:ns4="http://www.kith.no/xmlstds/msghead/2006-05-24" xmlns:ns3="http://www.kith.no/xmlstds/felleskomponent1">
            <ns4:MsgInfo>
                <ns4:Type V="DIALOG_NOTAT" DN="Notat"/>
                <ns4:MIGversion>v1.2 2006-05-24</ns4:MIGversion>
                <ns4:GenDate>2024-07-04T16:55:43.904207659</ns4:GenDate>
                <ns4:MsgId>ce3de801-9cae-4cb6-aa4e-b682cb6de3c9</ns4:MsgId>
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
            <ns4:Document>
                <ns4:DocumentConnection V="H" DN="Hoveddokument"/>
                <ns4:RefDoc>
                    <ns4:MsgType V="XML" DN="XML-instans"/>
                    <ns4:MimeType>text/xml</ns4:MimeType>
                    <ns4:Content>
                        <ns2:Dialogmelding>
                            <ns2:Notat>
                                <ns2:TemaKodet V="8" S="2.16.578.1.12.4.1.1.8127" DN="Melding fra NAV"/>
                                <ns2:TekstNotatInnhold>dette er litt tekst</ns2:TekstNotatInnhold>
                            </ns2:Notat>
                        </ns2:Dialogmelding>
                    </ns4:Content>
                </ns4:RefDoc>
            </ns4:Document>
        </ns4:MsgHead>
    """.trimIndent()
    }
}
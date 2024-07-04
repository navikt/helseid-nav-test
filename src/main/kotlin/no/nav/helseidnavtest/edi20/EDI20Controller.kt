package no.nav.helseidnavtest.edi20
import jakarta.xml.bind.Marshaller
import jakarta.xml.bind.Marshaller.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingGenerator
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.slf4j.LoggerFactory.getLogger
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.StringWriter
import java.util.*


@RestController(EDI20)
class EDI20Controller(private val a: EDI20RestClientAdapter, private val generator: DialogmeldingGenerator) {

    val xml = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <ns4:MsgHead xmlns="http://www.kith.no/xmlstds/base64container" xmlns:ns5="http://www.w3.org/2000/09/xmldsig#" xmlns:ns2="http://www.kith.no/xmlstds/dialog/2006-10-11" xmlns:ns4="http://www.kith.no/xmlstds/msghead/2006-05-24" xmlns:ns3="http://www.kith.no/xmlstds/felleskomponent1">
            <ns4:MsgInfo>
                <ns4:Type V="DIALOG_NOTAT" DN="Notat"/>
                <ns4:MIGversion>v1.2 2006-05-24</ns4:MIGversion>
                <ns4:GenDate>2024-07-04T16:55:43.904207659</ns4:GenDate>
                <ns4:MsgId>ce3de801-9cae-4cb6-aa4e-b682cb6de3c9</ns4:MsgId>
                <ns4:Sender>
                    <ns4:Organisation>
                        <ns4:OrganisationName>NAV</ns4:OrganisationName>
                        <ns4:Ident>
                            <ns4:Id>889640782</ns4:Id>
                            <ns4:TypeId V="ENH" S="2.16.578.1.12.4.1.1.9051" DN="Organisasjonsnummeret i Enhetsregisteret"/>
                        </ns4:Ident>
                        <ns4:Ident>
                            <ns4:Id>8142519</ns4:Id>
                            <ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret"/>
                        </ns4:Ident>
                    </ns4:Organisation>
                </ns4:Sender>
                <ns4:Receiver>
                    <ns4:Organisation>
                        <ns4:OrganisationName>SMESTAD LEGESENTER AS</ns4:OrganisationName>
                        <ns4:Ident>
                            <ns4:Id>8142520</ns4:Id>
                            <ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret"/>
                        </ns4:Ident>
                        <ns4:Ident>
                            <ns4:Id>997671694</ns4:Id>
                            <ns4:TypeId V="ENH" S="2.16.578.1.12.4.1.1.9051" DN="Organisasjonsnummeret i Enhetsregisteret"/>
                        </ns4:Ident>
                        <ns4:Address>
                            <ns4:Type V="RES" DN="Besøksadresse"/>
                            <ns4:StreetAdr>Sørkedalsveien 90b</ns4:StreetAdr>
                            <ns4:PostalCode>0377</ns4:PostalCode>
                            <ns4:City>OSLO</ns4:City>
                        </ns4:Address>
                        <ns4:HealthcareProfessional>
                            <ns4:FamilyName>VITS</ns4:FamilyName>
                            <ns4:MiddleName></ns4:MiddleName>
                            <ns4:GivenName>GRØNN</ns4:GivenName>
                            <ns4:Ident>
                                <ns4:Id>05898597468</ns4:Id>
                                <ns4:TypeId V="FNR" S="2.16.578.1.12.4.1.1.8116" DN="Fødselsnummer"/>
                            </ns4:Ident>
                            <ns4:Ident>
                                <ns4:Id>565501872</ns4:Id>
                                <ns4:TypeId V="HPR" S="2.16.578.1.12.4.1.1.8116" DN="HPR-nummer"/>
                            </ns4:Ident>
                            <ns4:Ident>
                                <ns4:Id>96588</ns4:Id>
                                <ns4:TypeId V="HER" S="2.16.578.1.12.4.1.1.8116" DN="Identifikator fra Helsetjenesteenhetsregisteret"/>
                            </ns4:Ident>
                        </ns4:HealthcareProfessional>
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
            <ns4:Document>
                <ns4:DocumentConnection V="V" DN="Vedlegg"/>
                <ns4:RefDoc>
                    <ns4:IssueDate V="2024-07-04"/>
                    <ns4:MsgType V="A" DN="Vedlegg"/>
                    <ns4:MimeType>application/pdf</ns4:MimeType>
                    <ns4:Content>
                        <Base64Container>JVBERi0xLjINJeLjz9MNCjMgMCBvYmoNPDwgDS9MaW5lYXJpemVkIDEgDS9PIDUgDS9IIFsgNzYwIDE1NyBdIA0vTCAzOTA4IA0vRSAzNjU4IA0vTiAxIA0vVCAzNzMxIA0+PiANZW5kb2JqDSAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB4cmVmDTMgMTUgDTAwMDAwMDAwMTYgMDAwMDAgbg0KMDAwMDAwMDY0NCAwMDAwMCBuDQowMDAwMDAwOTE3IDAwMDAwIG4NCjAwMDAwMDEwNjggMDAwMDAgbg0KMDAwMDAwMTIyNCAwMDAwMCBuDQowMDAwMDAxNDEwIDAwMDAwIG4NCjAwMDAwMDE1ODkgMDAwMDAgbg0KMDAwMDAwMTc2OCAwMDAwMCBuDQowMDAwMDAyMTk3IDAwMDAwIG4NCjAwMDAwMDIzODMgMDAwMDAgbg0KMDAwMDAwMjc2OSAwMDAwMCBuDQowMDAwMDAzMTcyIDAwMDAwIG4NCjAwMDAwMDMzNTEgMDAwMDAgbg0KMDAwMDAwMDc2MCAwMDAwMCBuDQowMDAwMDAwODk3IDAwMDAwIG4NCnRyYWlsZXINPDwNL1NpemUgMTgNL0luZm8gMSAwIFIgDS9Sb290IDQgMCBSIA0vUHJldiAzNzIyIA0vSURbPGQ3MGY0NmM1YmE0ZmU4YmQ0OWE5ZGQwNTk5YjBiMTUxPjxkNzBmNDZjNWJhNGZlOGJkNDlhOWRkMDU5OWIwYjE1MT5dDT4+DXN0YXJ0eHJlZg0wDSUlRU9GDSAgICAgIA00IDAgb2JqDTw8IA0vVHlwZSAvQ2F0YWxvZyANL1BhZ2VzIDIgMCBSIA0vT3BlbkFjdGlvbiBbIDUgMCBSIC9YWVogbnVsbCBudWxsIG51bGwgXSANL1BhZ2VNb2RlIC9Vc2VOb25lIA0+PiANZW5kb2JqDTE2IDAgb2JqDTw8IC9TIDM2IC9GaWx0ZXIgL0ZsYXRlRGVjb2RlIC9MZW5ndGggMTcgMCBSID4+IA1zdHJlYW0NCkiJYmBg4GVgYPrBAAScFxiwAQ4oLQDE3FDMwODHwKkyubctWLfmpsmimQ5AEYAAAwC3vwe0DWVuZHN0cmVhbQ1lbmRvYmoNMTcgMCBvYmoNNTMgDWVuZG9iag01IDAgb2JqDTw8IA0vVHlwZSAvUGFnZSANL1BhcmVudCAyIDAgUiANL1Jlc291cmNlcyA2IDAgUiANL0NvbnRlbnRzIDEwIDAgUiANL01lZGlhQm94IFsgMCAwIDYxMiA3OTIgXSANL0Nyb3BCb3ggWyAwIDAgNjEyIDc5MiBdIA0vUm90YXRlIDAgDT4+IA1lbmRvYmoNNiAwIG9iag08PCANL1Byb2NTZXQgWyAvUERGIC9UZXh0IF0gDS9Gb250IDw8IC9UVDIgOCAwIFIgL1RUNCAxMiAwIFIgL1RUNiAxMyAwIFIgPj4gDS9FeHRHU3RhdGUgPDwgL0dTMSAxNSAwIFIgPj4gDS9Db2xvclNwYWNlIDw8IC9DczUgOSAwIFIgPj4gDT4+IA1lbmRvYmoNNyAwIG9iag08PCANL1R5cGUgL0ZvbnREZXNjcmlwdG9yIA0vQXNjZW50IDg5MSANL0NhcEhlaWdodCAwIA0vRGVzY2VudCAtMjE2IA0vRmxhZ3MgMzQgDS9Gb250QkJveCBbIC01NjggLTMwNyAyMDI4IDEwMDcgXSANL0ZvbnROYW1lIC9UaW1lc05ld1JvbWFuIA0vSXRhbGljQW5nbGUgMCANL1N0ZW1WIDAgDT4+IA1lbmRvYmoNOCAwIG9iag08PCANL1R5cGUgL0ZvbnQgDS9TdWJ0eXBlIC9UcnVlVHlwZSANL0ZpcnN0Q2hhciAzMiANL0xhc3RDaGFyIDMyIA0vV2lkdGhzIFsgMjUwIF0gDS9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIA0vQmFzZUZvbnQgL1RpbWVzTmV3Um9tYW4gDS9Gb250RGVzY3JpcHRvciA3IDAgUiANPj4gDWVuZG9iag05IDAgb2JqDVsgDS9DYWxSR0IgPDwgL1doaXRlUG9pbnQgWyAwLjk1MDUgMSAxLjA4OSBdIC9HYW1tYSBbIDIuMjIyMjEgMi4yMjIyMSAyLjIyMjIxIF0gDS9NYXRyaXggWyAwLjQxMjQgMC4yMTI2IDAuMDE5MyAwLjM1NzYgMC43MTUxOSAwLjExOTIgMC4xODA1IDAuMDcyMiAwLjk1MDUgXSA+PiANDV0NZW5kb2JqDTEwIDAgb2JqDTw8IC9MZW5ndGggMzU1IC9GaWx0ZXIgL0ZsYXRlRGVjb2RlID4+IA1zdHJlYW0NCkiJdJDBTsMwEETv/oo92ohuvXHsJEeggOCEwDfEIU1SCqIJIimIv2dthyJVQpGc0Xo88+xzL5beZ0DgN4IIq6oCzd8sK43amAyK3GKmTQV+J5YXo4VmjDYNYyOW1w8Ez6PQ4JuwfAkJyr+yXNgSSwt+NU+4Kp+rcg4uy9Q1a6MdarLcpgvUeUGh7RBFSLk1f1n+5FgsHJaZttFqA+tKLJhfZ3kEY+VcoHuUfvui2O3kCL9COSwk1Ok3deMEd6srUCVa2Q7Nftf1Ewar5a4nfxuu4v59NcLMGAKXlcjMLtwj1BsTQCITUSK52cC3IoNGDnto6l5VmEv4YAwjO8VWJ+s2DSeGttw/qmA/PZyLu3vY1p9p0MGZIs2iHdZxjwdNSkzedT0pJiW+CWl5H0O7uu2SB1JLn8rHlMkH2F+/xa20Rjp+nAQ39Ec8c1gz7KJ4T3H7uXnuwvSWl178CDAA/bGPlAplbmRzdHJlYW0NZW5kb2JqDTExIDAgb2JqDTw8IA0vVHlwZSAvRm9udERlc2NyaXB0b3IgDS9Bc2NlbnQgOTA1IA0vQ2FwSGVpZ2h0IDAgDS9EZXNjZW50IC0yMTEgDS9GbGFncyAzMiANL0ZvbnRCQm94IFsgLTYyOCAtMzc2IDIwMzQgMTA0OCBdIA0vRm9udE5hbWUgL0FyaWFsLEJvbGQgDS9JdGFsaWNBbmdsZSAwIA0vU3RlbVYgMTMzIA0+PiANZW5kb2JqDTEyIDAgb2JqDTw8IA0vVHlwZSAvRm9udCANL1N1YnR5cGUgL1RydWVUeXBlIA0vRmlyc3RDaGFyIDMyIA0vTGFzdENoYXIgMTE3IA0vV2lkdGhzIFsgMjc4IDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMjc4IDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgDTAgMCAwIDAgMCA3MjIgMCA2MTEgMCAwIDAgMCAwIDAgMCAwIDAgNjY3IDAgMCAwIDYxMSAwIDAgMCAwIDAgMCANMCAwIDAgMCAwIDAgNTU2IDAgNTU2IDYxMSA1NTYgMCAwIDYxMSAyNzggMCAwIDAgODg5IDYxMSA2MTEgMCAwIA0wIDU1NiAzMzMgNjExIF0gDS9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIA0vQmFzZUZvbnQgL0FyaWFsLEJvbGQgDS9Gb250RGVzY3JpcHRvciAxMSAwIFIgDT4+IA1lbmRvYmoNMTMgMCBvYmoNPDwgDS9UeXBlIC9Gb250IA0vU3VidHlwZSAvVHJ1ZVR5cGUgDS9GaXJzdENoYXIgMzIgDS9MYXN0Q2hhciAxMjEgDS9XaWR0aHMgWyAyNzggMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDI3OCAwIDI3OCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCANMCAwIDAgNjY3IDAgMCAwIDAgMCAwIDAgMjc4IDAgMCAwIDAgMCAwIDAgMCA3MjIgMCAwIDAgMCAwIDAgMCAwIA0wIDAgMCAwIDAgMCA1NTYgNTU2IDUwMCA1NTYgNTU2IDI3OCAwIDU1NiAyMjIgMCAwIDIyMiA4MzMgNTU2IDU1NiANNTU2IDAgMzMzIDUwMCAyNzggNTU2IDUwMCAwIDAgNTAwIF0gDS9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nIA0vQmFzZUZvbnQgL0FyaWFsIA0vRm9udERlc2NyaXB0b3IgMTQgMCBSIA0+PiANZW5kb2JqDTE0IDAgb2JqDTw8IA0vVHlwZSAvRm9udERlc2NyaXB0b3IgDS9Bc2NlbnQgOTA1IA0vQ2FwSGVpZ2h0IDAgDS9EZXNjZW50IC0yMTEgDS9GbGFncyAzMiANL0ZvbnRCQm94IFsgLTY2NSAtMzI1IDIwMjggMTAzNyBdIA0vRm9udE5hbWUgL0FyaWFsIA0vSXRhbGljQW5nbGUgMCANL1N0ZW1WIDAgDT4+IA1lbmRvYmoNMTUgMCBvYmoNPDwgDS9UeXBlIC9FeHRHU3RhdGUgDS9TQSBmYWxzZSANL1NNIDAuMDIgDS9UUiAvSWRlbnRpdHkgDT4+IA1lbmRvYmoNMSAwIG9iag08PCANL1Byb2R1Y2VyIChBY3JvYmF0IERpc3RpbGxlciA0LjA1IGZvciBXaW5kb3dzKQ0vQ3JlYXRvciAoTWljcm9zb2Z0IFdvcmQgOS4wKQ0vTW9kRGF0ZSAoRDoyMDAxMDgyOTA5NTUwMS0wNycwMCcpDS9BdXRob3IgKEdlbmUgQnJ1bWJsYXkpDS9UaXRsZSAoVGhpcyBpcyBhIHRlc3QgUERGIGRvY3VtZW50KQ0vQ3JlYXRpb25EYXRlIChEOjIwMDEwODI5MDk1NDU3KQ0+PiANZW5kb2JqDTIgMCBvYmoNPDwgDS9UeXBlIC9QYWdlcyANL0tpZHMgWyA1IDAgUiBdIA0vQ291bnQgMSANPj4gDWVuZG9iag14cmVmDTAgMyANMDAwMDAwMDAwMCA2NTUzNSBmDQowMDAwMDAzNDI5IDAwMDAwIG4NCjAwMDAwMDM2NTggMDAwMDAgbg0KdHJhaWxlcg08PA0vU2l6ZSAzDS9JRFs8ZDcwZjQ2YzViYTRmZThiZDQ5YTlkZDA1OTliMGIxNTE+PGQ3MGY0NmM1YmE0ZmU4YmQ0OWE5ZGQwNTk5YjBiMTUxPl0NPj4Nc3RhcnR4cmVmDTE3Mw0lJUVPRg0=</Base64Container>
                    </ns4:Content>
                </ns4:RefDoc>
            </ns4:Document>
        </ns4:MsgHead>
    """.trimIndent()
    protected val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages() = a.messages()

    @GetMapping("/dialogmelding") fun dialogmelding(@RequestParam pasient: Fødselsnummer): String {
        try {
            log.info("Genererer melding")
            val msg = generator.genererDialogmelding(pasient, UUID.randomUUID()).also {
                log.info("Melding generert: {}", it)
            }
            val writer = StringWriter()
            val hodemelding = msg.any.first { it is XMLMsgHead } as XMLMsgHead
            log.info("Marshalling XML ")
            Jaxb2Marshaller().apply {
                setMarshallerProperties(mapOf(JAXB_FORMATTED_OUTPUT to true))
                setClassesToBeBound(
                    XMLBase64Container::class.java,
                    XMLDialogmelding::class.java,
                    XMLMsgHead::class.java)
            }.createMarshaller().marshal(hodemelding, writer)
            log.info("XML {}", xml)
            val b64 = Base64.getEncoder().encodeToString(xml.toByteArray())
            val bd = EDI20DTOs.BusinessDocument(b64, EDI20DTOs.Properties(EDI20DTOs.System("HelseIdNavTest", "1.0.0")))
            a.postMessage(bd)
            return "OK"
        }
        catch (e: Exception) {
            log.error("Feil ved generering av dialogmelding", e)
            return "NOT OK"
        }

    }
}
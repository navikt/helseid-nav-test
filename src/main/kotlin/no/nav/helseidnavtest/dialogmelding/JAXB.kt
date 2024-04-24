package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.*
import no.nav.helseidnavtest.dialogmelding.MsgHeaderUtil.HMOF
import no.nav.helseidnavtest.dialogmelding.MsgHeaderUtil.mottaker
import no.nav.helseidnavtest.dialogmelding.MsgHeaderUtil.pasient
import no.nav.helseidnavtest.dialogmelding.MsgHeaderUtil.avsender
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.FFOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseidnavtest.oppslag.arbeid.Fødselsnummer
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import java.math.BigInteger.*
import java.time.LocalDate
import java.time.LocalDateTime.*
import java.time.format.DateTimeFormatter.*
import java.util.UUID.*
import java.util.function.Function

val apprecJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java)

val apprecUnmarshaller: Unmarshaller = apprecJaxBContext.createUnmarshaller().apply {

    // TODO setAdapter(XMLDateTimeAdapter::class.java, XMLDateTimeAdapter())
   // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
}

object ObjectFactories{
    val DMOF = no.nav.helseopplysninger.dialogmelding.ObjectFactory()
    val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
    val VOF = no.nav.helseopplysninger.basecontainer.ObjectFactory()
}


object JAXB{
    fun createFellesformat(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        FFOF.createXMLEIFellesformat().apply {
            with(any) {
                add(msgHead(melding, arbeidstaker))
                add(mottakenhetBlokk(melding))
                add(sporinformasjonBlokk())
            }
        }
}

fun msgHead(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
    HMOF.createXMLMsgHead().apply {
        msgInfo = msgInfo(melding, arbeidstaker)
        document.add(dialogmeldingDocument(melding))
        document.add(vedleggDocument(melding))
}

fun mottakenhetBlokk(melding: Dialogmelding) =
    with(melding) {
        if (DIALOG_NOTAT to HENVENDELSE != type to kodeverk)  {
            throw IllegalArgumentException("Ugyldig melding/type kombinasjon $type/$kodeverk")
        }
        FFOF.createXMLMottakenhetBlokk().apply {
            partnerReferanse = behandler.kontor.partnerId.value.toString()
            ebRole = "Saksbehandler"
            ebService =  "HenvendelseFraSaksbehandler"
            ebAction = "Henvendelse"
        }
    }

fun sporinformasjonBlokk() =
    FFOF.createSporinformasjonBlokk(FFOF.createXMLSporinformasjonBlokkType().apply {
        programID = "Helseopplysninger webapp"
        programVersjonID = "1.0"
        programResultatKoder = ZERO
        tidsstempel = now()
    })

fun msgInfo(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
    HMOF.createXMLMsgInfo().apply {
        type = HMOF.createXMLCS().apply {
            dn = "Notat"
            v = DIALOG_NOTAT.name
            miGversion = "v1.2 2006-05-24"
            genDate = now()
            msgId = melding.uuid.toString()
            ack = HMOF.createXMLCS().apply {
                dn = "Ja"
                v = "J"
            }
            /*
            conversationRef = HMOF.createXMLConversationRef().apply {
                with(melding) {
                    refToConversation = "$conversationUuid"
                    refToParent = parentRef ?: "$conversationUuid"
                }
            } */
        }
        sender = avsender()
        receiver = mottaker(melding)
        patient = pasient(arbeidstaker)
    }


fun vedleggDocument(melding: Dialogmelding) =
    HMOF.createXMLDocument().apply {
        documentConnection = HMOF.createXMLCS().apply {
            dn = "Vedlegg"
            v = "V"
        }
        refDoc = HMOF.createXMLRefDoc().apply {
            issueDate = HMOF.createXMLTS().apply {
                v = LocalDate.now().format(ISO_DATE)
            }
            msgType = HMOF.createXMLCS().apply {
                dn = "Vedlegg"
                v = "A"
            }
            mimeType = APPLICATION_PDF_VALUE
            content = HMOF.createXMLRefDocContent().apply {
                any.add(VOF.createXMLBase64Container().apply {
                    value = melding.vedlegg
                })
            }
        }
    }
fun dialogmeldingDocument(melding: Dialogmelding) =
    HMOF.createXMLDocument().apply {
        documentConnection = HMOF.createXMLCS().apply {
            dn = "Hoveddokument"
            v = "H"
        }
        refDoc = HMOF.createXMLRefDoc().apply {
            HMOF.createXMLTS().apply {
                v = LocalDate.now().format(ISO_DATE)
            }
            msgType = HMOF.createXMLCS().apply {
                dn = "XML-instans"
                v = "XML"
            }
            mimeType = TEXT_XML_VALUE
            content = HMOF.createXMLRefDocContent().apply {
                any.add(xmlFra(melding))
            }
        }
    }

fun xmlFra(melding: Dialogmelding) =
    with(melding) {
        require(type == DIALOG_NOTAT) { "Kan ikke lage dialogmelding, ukjent type '$type'" }
        require(kodeverk != null) { "Kan ikke lage dialogmelding, kodeverk ikke satt" }
        DMOF.createXMLDialogmelding().apply {
            notat.add(DMOF.createXMLNotat().apply {
                temaKodet = DMOF.createCV().apply {
                    s = kodeverk.id
                    v = "${kode.value}"
                    dn = "Melding fra NAV"
                }
                tekstNotatInnhold = tekst
                dokIdNotat = "${randomUUID()}"
            })
        }
    }

fun createXMLIdentForPersonident(fnr: Fødselsnummer) =
    HMOF.createXMLIdent().apply {
        id = fnr.fnr
        typeId = HMOF.createXMLCV().apply {
            dn = if (fnr.type == "DNR") "D-nummer" else "Fødselsnummer"
            s = "2.16.578.1.12.4.1.1.8116"
            v = fnr.type
        }
    }
class Fellesformat(fellesformat: XMLEIFellesformat, marshaller: Function<XMLEIFellesformat, String>)  {
        val message = marshaller.apply(fellesformat)
}




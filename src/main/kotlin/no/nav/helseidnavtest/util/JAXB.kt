package no.nav.helseidnavtest.util

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import no.nav.helseidnavtest.domain.*
import no.nav.helseidnavtest.domain.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.domain.DialogmeldingType.*
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import java.math.BigInteger.*
import java.time.LocalDate
import java.time.LocalDateTime.*
import java.time.format.DateTimeFormatter.*
import java.util.*
import java.util.function.Function

val apprecJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java)

val apprecUnmarshaller: Unmarshaller = apprecJaxBContext.createUnmarshaller().apply {

    // TODO setAdapter(XMLDateTimeAdapter::class.java, XMLDateTimeAdapter())
   // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
}

val DMOF = no.nav.helseopplysninger.dialogmelding.ObjectFactory()
val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
val HMOF =  no.nav.helseopplysninger.hodemelding.ObjectFactory()
val VOF = no.nav.helseopplysninger.basecontainer.ObjectFactory()

object JAXB{
    fun createFellesformat(melding: DialogmeldingBestilling, arbeidstaker: Arbeidstaker) =
        FFOF.createXMLEIFellesformat().apply {
            any.add(msgHead(melding, arbeidstaker))
            any.add(mottakenhetBlokk(melding))
            any.add(sporinformasjonBlokk())
        }
}

fun msgHead(melding: DialogmeldingBestilling, arbeidstaker: Arbeidstaker) =
    HMOF.createXMLMsgHead().apply {
        msgInfo = msgInfo(melding, arbeidstaker)
        document.add(dialogmeldingDocument(melding))
        document.add(vedleggDocument(melding))
}
fun mottakenhetBlokk(melding: DialogmeldingBestilling) =
    with(melding) {
        if (DIALOG_NOTAT to HENVENDELSE != type to kodeverk)  {
            throw IllegalArgumentException("Invalid melding type/kodeverk-combinatio $type/$kodeverk")
        }
        val storedPartnerId = behandler.kontor.partnerId
        val partnerId = if (storedPartnerId.value in listOf(14859, 41578)) "60274" else "${storedPartnerId.value}"
         FFOF.createXMLMottakenhetBlokk().apply {
            partnerReferanse = partnerId
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

fun vedleggDocument(melding: DialogmeldingBestilling) =
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
fun dialogmeldingDocument(melding: DialogmeldingBestilling) =
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
                any.add(createDialogmelding(melding))
            }
        }
    }

fun createDialogmelding(melding: DialogmeldingBestilling) =
    with(melding) {
        require(type == DIALOG_NOTAT) { "Cannot send dialogmelding, unknown type: $type" }
            require(kodeverk != null) { "Cannot send dialogmelding when kodeverk is missing" }
            DMOF.createXMLDialogmelding().apply {
                notat.add(DMOF.createXMLNotat().apply {
                        temaKodet = DMOF.createCV().apply {
                            s = kodeverk.kodeverkId
                            v = kode.value.toString()
                            dn = "Melding fra NAV"
                        }
                        tekstNotatInnhold = tekst
                        dokIdNotat = UUID.randomUUID().toString()
                    })
            }
    }


fun msgInfo(melding: DialogmeldingBestilling, arbeidstaker: Arbeidstaker) =
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
            conversationRef = HMOF.createXMLConversationRef().apply {
                with(melding) {
                    refToConversation = "$conversationUuid"
                    refToParent = parentRef ?: "$conversationUuid"
                }
            }
        }
        sender = createSender()
        receiver = createReceiver(melding)
        patient = createPasient(arbeidstaker)
    }

fun createSender() =
    HMOF.createXMLSender().apply {
        organisation = HMOF.createXMLOrganisation().apply {
            organisationName = "NAV"
            ident.add(HMOF.createXMLIdent().apply {
                id = "889640782"
                typeId = HMOF.createXMLCV().apply {
                    dn = "Organisasjonsnummeret i Enhetsregisteret"
                    s = "2.16.578.1.12.4.1.1.9051"
                    v = "ENH"
                }
            })
            ident.add(HMOF.createXMLIdent().apply {
                id = "79768"
                typeId = HMOF.createXMLCV().apply {
                    dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                    s = "2.16.578.1.12.4.1.1.9051"
                    v = "HER"
                }
            })
        }
    }
fun createReceiver(bestiling: DialogmeldingBestilling) =
    HMOF.createXMLReceiver().apply {
        with(bestiling.behandler)  {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = kontor.navn
                ident.add(HMOF.createXMLIdent().apply {
                    id = kontor.herId.toString()
                    typeId = HMOF.createXMLCV().apply {
                        dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "HER"
                    }
                })
                kontor.orgnummer?.let { orgnummer ->
                    ident.add(HMOF.createXMLIdent().apply {
                        id = orgnummer.value
                        typeId = HMOF.createXMLCV().apply {
                            dn = "Organisasjonsnummeret i Enhetsregisteret"
                            s = "2.16.578.1.12.4.1.1.9051"
                            v = "ENH"
                        }
                    })
                }
                address = HMOF.createXMLAddress().apply {
                    type = HMOF.createXMLCS().apply {
                        dn = "Besøksadresse"
                        v = "RES"
                    }
                    streetAdr = kontor.adresse
                    postalCode = kontor.postnummer
                    city = kontor.poststed
                }
                healthcareProfessional = HMOF.createXMLHealthcareProfessional().apply {
                    familyName = etternavn
                    middleName = mellomnavn
                    givenName = fornavn
                    personident?.let {
                        ident.add(createXMLIdentForPersonident(it))
                    }
                    hprId?.let {
                        ident.add(HMOF.createXMLIdent().apply {
                            id = "$it"
                            typeId = HMOF.createXMLCV().apply {
                                dn = "HPR-nummer"
                                s = "2.16.578.1.12.4.1.1.8116"
                                v = "HPR"
                            }
                        })
                    }
                    herId?.let {
                        ident.add(HMOF.createXMLIdent().apply {
                            id = "$it"
                            typeId = HMOF.createXMLCV().apply {
                                dn = "Identifikator fra Helsetjenesteenhetsregisteret"
                                s = "2.16.578.1.12.4.1.1.8116"
                                v = "HER"
                            }
                        })
                    }
                }
            }
        }
    }

fun createPasient(arbeidstaker: Arbeidstaker) =
    HMOF.createXMLPatient().apply {
        with(arbeidstaker) {
            familyName = etternavn
            middleName = mellomnavn
            givenName = fornavn
            ident.add(createXMLIdentForPersonident(arbeidstakerPersonident))
        }
    }
fun createXMLIdentForPersonident(personident: Personident) =
    HMOF.createXMLIdent().apply {
        id = personident.value
        typeId = HMOF.createXMLCV().apply {
            dn = if (personident.type == "DNR") "D-nummer" else "Fødselsnummer"
            s = "2.16.578.1.12.4.1.1.8116"
            v = personident.type
        }
    }
class Fellesformat(fellesformat: XMLEIFellesformat, marshaller: Function<XMLEIFellesformat?, String?>)  {
        val message = marshaller.apply(fellesformat)
}




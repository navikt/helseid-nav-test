package no.nav.helseidnavtest.dialogmelding

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.HENVENDELSE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.DIALOG_NOTAT
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.FFOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import org.springframework.stereotype.Component
import java.io.StringWriter
import java.math.BigInteger.ZERO
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.UUID.randomUUID
import java.util.function.Function
import javax.xml.transform.stream.StreamResult

@Component
class DialogmeldingMapper {
    private val MARSHALLER = JAXBContext.newInstance(
        XMLEIFellesformat::class.java,
        XMLMsgHead::class.java,
        XMLDialogmelding::class.java,
        XMLBase64Container::class.java,
        XMLSporinformasjonBlokkType::class.java).createMarshaller().apply {
            setProperty(JAXB_FORMATTED_OUTPUT, true)
            setProperty(JAXB_ENCODING, "UTF-8")
        }

    fun xmlFra(melding: Dialogmelding, arbeidstaker: Arbeidstaker) = Fellesformat(createFellesformat(melding, arbeidstaker), ::marshall)

    private fun marshall(element: Any?) =
        run {
            val writer = StringWriter()
            MARSHALLER.marshal(element, StreamResult(writer))
            "$writer"
        }

    private fun createFellesformat(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        FFOF.createXMLEIFellesformat().apply {
            with(any) {
                add(hodemelding(melding, arbeidstaker))
                add(mottakenhetBlokk(melding))
                add(sporinformasjonBlokk())
            }
        }


    private fun hodemelding(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        HMOF.createXMLMsgHead().apply {
            msgInfo = msgInfo(melding, arbeidstaker)
            document.add(dialogmeldingDocument(melding))
            document.add(vedleggDocument(melding))
        }
    private fun mottakenhetBlokk(melding: Dialogmelding) =
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

    private fun sporinformasjonBlokk() =
        FFOF.createSporinformasjonBlokk(FFOF.createXMLSporinformasjonBlokkType().apply {
            programID = "Helseopplysninger webapp"
            programVersjonID = "1.0"
            programResultatKoder = ZERO
            tidsstempel = now()
        })

    private fun msgInfo(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        HMOF.createXMLMsgInfo().apply {
            type = HMOF.createXMLCS().apply {
                dn = "Notat"
                v = DIALOG_NOTAT.name
                miGversion = "v1.2 2006-05-24"
                genDate = now()
                msgId = melding.uuid.toString()
            }
            sender = avsender()
            receiver = mottaker(melding)
            patient = pasient(arbeidstaker)
        }

    private fun vedleggDocument(melding: Dialogmelding) =
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


    private fun dialogmeldingDocument(melding: Dialogmelding) =
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
    private fun xmlFra(melding: Dialogmelding) =
        with(melding) {
            require(type == DIALOG_NOTAT) { "Kan ikke lage dialogmelding, ukjent type '$type'" }
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
    private fun createXMLIdentForPersonident(fnr: Fødselsnummer) =
        HMOF.createXMLIdent().apply {
            id = fnr.value
            typeId = HMOF.createXMLCV().apply {
                dn = if (fnr.type == "DNR") "D-nummer" else "Fødselsnummer"
                s = "2.16.578.1.12.4.1.1.8116"
                v = fnr.type
            }
        }

    private fun avsender() =
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

    private fun pasient(arbeidstaker: Arbeidstaker) =
        HMOF.createXMLPatient().apply {
            with(arbeidstaker) {
                familyName = etternavn
                middleName = mellomnavn
                givenName = fornavn
                ident.add(createXMLIdentForPersonident(arbeidstakerPersonident))
            }
        }

    private fun mottaker(bestiling: Dialogmelding) =
        HMOF.createXMLReceiver().apply {
            with(bestiling.behandler)  {
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = kontor.navn
                    ident.add(HMOF.createXMLIdent().apply {
                        id = "${kontor.herId}"
                        typeId = HMOF.createXMLCV().apply {
                            dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                            s = "2.16.578.1.12.4.1.1.9051"
                            v = "HER"
                        }
                    })
                    kontor.orgnummer?.let { orgnummer ->
                        ident.add(HMOF.createXMLIdent().apply {
                            id = orgnummer.value.toString()
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
    data class Fellesformat(val fellesformat: XMLEIFellesformat, val marshaller: Function<XMLEIFellesformat, String>)  {
        val message = marshaller.apply(fellesformat)
    }
}
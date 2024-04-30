package no.nav.helseidnavtest.dialogmelding


import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.HENVENDELSE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.DIALOG_NOTAT
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.FFOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.hodemelding.XMLIdent
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
class DialogmeldingMapper(private val adresseWSAdapter: AdresseWSAdapter) {

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
                val hm = hodemelding(melding, arbeidstaker)
                add(hm)
                add(mottakenhetBlokk(melding,hm.msgInfo.receiver.organisation.ident.find { it.typeId.v == "HER" }!!.id))
                add(sporinformasjonBlokk())
            }
        }


    private fun hodemelding(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        HMOF.createXMLMsgHead().apply {
            msgInfo = msgInfo(melding, arbeidstaker)
            document.add(dialogmeldingDocument(melding))
            document.add(vedleggDocument(melding))
        }
    private fun mottakenhetBlokk(melding: Dialogmelding, herId: String) =
        with(melding) {
            if (DIALOG_NOTAT to HENVENDELSE != type to kodeverk)  {
                throw IllegalArgumentException("Ugyldig melding/type kombinasjon $type/$kodeverk")
            }
            FFOF.createXMLMottakenhetBlokk().apply {
                ebRole = "Saksbehandler"
                herIdentifikator = herId
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
                msgId = "${melding.uuid}"
            }
            sender = avsenderNAV()
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
                dn = fnr.type.verdi
                s = "2.16.578.1.12.4.1.1.8116"
                v = fnr.type.name
            }
        }

    private fun avsenderNAV() =
        HMOF.createXMLSender().apply {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = "NAV"
                ident.add(HMOF.createXMLIdent().apply {
                    id = "$NAV_ORGNR"
                    typeId = HMOF.createXMLCV().apply {
                        dn = "Organisasjonsnummeret i Enhetsregisteret"
                        s = NAV_OID
                        v = "ENH"
                    }
                })
                ident.add(HMOF.createXMLIdent().apply {
                    id = adresseWSAdapter.herIdForVirksomhet(Virksomhetsnummer(NAV_ORGNR)).toString()   //"79768"
                    typeId = HMOF.createXMLCV().apply {
                        dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                        s = NAV_OID
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
                    kontor.orgnummer.let { orgnummer ->
                        ident.add(HMOF.createXMLIdent().apply {
                            id = orgnummer.verdi
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
    companion object {
        private val NAV_ORGNR  = 889640782
        private val NAV_OID = "2.16.578.1.12.4.1.1.9051"
    }
    data class Fellesformat(val fellesformat: XMLEIFellesformat, val marshaller: Function<XMLEIFellesformat, String>)  {
        val message = marshaller.apply(fellesformat)
    }

}
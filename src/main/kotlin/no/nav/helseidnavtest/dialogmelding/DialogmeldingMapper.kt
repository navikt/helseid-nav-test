package no.nav.helseidnavtest.dialogmelding


import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.HENVENDELSE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.DIALOG_NOTAT
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.FFOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseidnavtest.oppslag.adresse.AdresseWSAdapter
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
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

    fun xmlFra(melding: Dialogmelding, arbeidstaker: Arbeidstaker) = Fellesformat(createFellesformat(melding, arbeidstaker), ::marshall).xml

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
    private fun ident(fnr: Fødselsnummer) =
        HMOF.createXMLIdent().apply {
            id = fnr.value
            typeId = type(HER_OID, fnr.type.name, fnr.type.verdi)
        }

    private fun avsenderNAV() =
        HMOF.createXMLSender().apply {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = "NAV"
                ident.add(HMOF.createXMLIdent().apply {
                    id = NAV_ORGNR.verdi
                    typeId = type(NAV_OID, ENH, ENHET_DESC)
                })
                ident.add(idFra(adresseWSAdapter.herIdForVirksomhet(NAV_ORGNR).verdi,type(NAV_OID, HER, HER_DESC)))
            }
        }

    private fun pasient(arbeidstaker: Arbeidstaker) =
        HMOF.createXMLPatient().apply {
            with(arbeidstaker) {
                familyName = etternavn
                middleName = mellomnavn
                givenName = fornavn
                ident.add(ident(arbeidstakerPersonident))
            }
        }

    private fun mottaker(bestiling: Dialogmelding) =
        HMOF.createXMLReceiver().apply {
            with(bestiling.behandler)  {
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = kontor.navn
                    ident.add(HMOF.createXMLIdent().apply {
                        id = "${kontor.herId}"
                        typeId = type(NAV_OID, HER, HER_DESC)
                    })
                    kontor.orgnummer.let { orgnummer ->
                        ident.add(HMOF.createXMLIdent().apply {
                            id = orgnummer.verdi
                            typeId = type(NAV_OID, ENH, ENHET_DESC)
                        })
                    }
                    address = HMOF.createXMLAddress().apply {
                        type = HMOF.createXMLCS().apply {
                            dn = "Besøksadresse"
                            v = "RES"
                        }
                        with(kontor) {
                            streetAdr = adresse
                            postalCode = postnummer.verdi
                            city = poststed
                        }
                    }
                    healthcareProfessional = HMOF.createXMLHealthcareProfessional().apply {
                        familyName = etternavn
                        middleName = mellomnavn
                        givenName = fornavn
                        personident?.let {
                            ident.add(ident(it))
                        }
                        hprId?.let {
                            ident.add(HMOF.createXMLIdent().apply {
                                id = "$it"
                                typeId = type(HER_OID, HPR, "HPR-nummer")
                            })
                        }
                        herId?.let {
                            ident.add(HMOF.createXMLIdent().apply {
                                id = "$it"
                                typeId = type(HER_OID, HER, HER_DESC)
                            })
                        }
                    }
                }
            }
        }

    companion object {
        private const val ENHET_DESC = "Organisasjonsnummeret i Enhetsregisteret"
        private const val HER_DESC = "Identifikator fra Helsetjenesteenhetsregisteret"
        private val NAV_ORGNR  = Virksomhetsnummer(889640782)
        private const val NAV_OID = "2.16.578.1.12.4.1.1.9051"
        private const val HER_OID ="2.16.578.1.12.4.1.1.8116"
        private const val ENH = "ENH"
        private const val HER = "HER"
        private const val HPR = "HPR"
    }
    private  data class Fellesformat(private val fellesformat: XMLEIFellesformat, private val marshaller: Function<XMLEIFellesformat, String>)  {
        val xml = marshaller.apply(fellesformat)
    }


}
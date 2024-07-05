package no.nav.helseidnavtest.dialogmelding


import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.HENVENDELSE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.DIALOG_NOTAT
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.FFOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.hodemelding.XMLCV
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import org.springframework.stereotype.Component
import java.math.BigInteger.ZERO
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.function.Function

@Component
class DialogmeldingMapper(private val adresse: AdresseRegisterClient) {

    fun fellesFormat(melding: Dialogmelding, arbeidstaker: Arbeidstaker) = createFellesformat(melding, arbeidstaker)

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
            document.addAll(listOf(dialogmeldingDocument(melding), vedleggDocument(melding)))
        }
    private fun mottakenhetBlokk(melding: Dialogmelding) =
        with(melding) {
            if (DIALOG_NOTAT to HENVENDELSE != type to kodeverk)  {
                throw IllegalArgumentException("Ugyldig melding/type kombinasjon $type/$kodeverk")
            }
            FFOF.createXMLMottakenhetBlokk().apply {
                ebRole = EBROLE
                //partnerReferanse = melding.behandler.kontor.partnerId!!.value
                ebService =  EBSERVICE
                ebAction = EBACTION
            }
        }

    private fun sporinformasjonBlokk() =
        FFOF.createSporinformasjonBlokk(FFOF.createXMLSporinformasjonBlokkType().apply {
            programID = "Helseopplysninger Webapp"
            programVersjonID = "1.0"
            programResultatKoder = ZERO
            tidsstempel = now()
        })

    private fun msgInfo(melding: Dialogmelding, arbeidstaker: Arbeidstaker) =
        HMOF.createXMLMsgInfo().apply {
            type = HMOF.createXMLCS().apply {
                dn = "Notat"
                v = DIALOG_NOTAT.name
                miGversion = VERSION
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
                dn = VEDLEGG
                v = "V"
            }
            refDoc = HMOF.createXMLRefDoc().apply {
                issueDate = HMOF.createXMLTS().apply {
                    v = LocalDate.now().format(ISO_DATE)
                }
                msgType = HMOF.createXMLCS().apply {
                    dn = VEDLEGG
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
                })
            }
        }
    private fun ident(fnr: Fødselsnummer) =
        HMOF.createXMLIdent().apply {
            id = fnr.verdi
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
                ident.add(idFra(adresse.herIdForOrgnummer(NAV_ORGNR).verdi,type(NAV_OID, HER, HER_DESC)))
            }
        }

    private fun pasient(arbeidstaker: Arbeidstaker) =
        HMOF.createXMLPatient().apply {
            with(arbeidstaker) {
                familyName = navn.etternavn
                navn.mellomnavn?.let { middleName = it }
                givenName = navn.fornavn
                ident.add(ident(id))
            }
        }

    private fun mottaker(bestiling: Dialogmelding) =
        HMOF.createXMLReceiver().apply {
            with(bestiling.behandler)  {
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = kontor.navn
                    ident.add(HMOF.createXMLIdent().apply {
                        id = kontor.herId?.verdi ?: throw IllegalArgumentException("Mangler HER-id")
                        typeId = type(NAV_OID, HER, HER_DESC)
                    })

                    ident.add(HMOF.createXMLIdent().apply {
                        id =  kontor.orgnummer.verdi
                        typeId = type(NAV_OID, ENH, ENHET_DESC)
                    })

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
                        familyName = navn.etternavn
                        middleName = navn.mellomnavn
                        givenName = navn.fornavn
                        personident?.let {
                            ident.add(ident(it))
                        }
                        ident.add(HMOF.createXMLIdent().apply {
                            id = hprId.verdi
                            typeId = type(HER_OID, HPR, "HPR-nummer")
                        })

                        ident.add(HMOF.createXMLIdent().apply {
                            id = herId.verdi
                            typeId = type(HER_OID, HER, HER_DESC)
                        })

                    }
                }
            }
        }

    companion object {
        private const val VEDLEGG = "Vedlegg"
        private const val VERSION = "v1.2 2006-05-24"
        const val EBROLE = "Sykmelder"
        const val EBSERVICE = "HenvendelseFraSaksbehandler"
        const val EBACTION = "Henvendelse"
        private const val ENHET_DESC = "Organisasjonsnummeret i Enhetsregister"
        private const val HER_DESC = "HER-id"
        private val NAV_ORGNR  = Orgnummer(889640782)
        private const val NAV_OID = "2.16.578.1.12.4.1.1.9051"
        private const val HER_OID ="2.16.578.1.12.4.1.1.8116"
        private const val ENH = "ENH"
        private const val HER = "HER"
        private const val HPR = "HPR"
    }
    private  data class Fellesformat(private val fellesformat: XMLEIFellesformat, private val marshaller: Function<XMLEIFellesformat, String>)  {
        val xml = marshaller.apply(fellesformat)
    }
    fun idFra(id: String, typeId: XMLCV) = HMOF.createXMLIdent().apply {
        this.id = id
        this.typeId = typeId
    }

    fun type(s: String, v: String, dn: String) =
        XMLCV().apply {
            this.s = s
            this.v = v
            this.dn = dn
        }

}
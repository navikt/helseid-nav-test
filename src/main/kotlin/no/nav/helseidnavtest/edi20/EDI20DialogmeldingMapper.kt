package no.nav.helseidnavtest.edi20

import no.nav.helseidnavtest.dialogmelding.Dialogmelding
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKode.KODE8
import no.nav.helseidnavtest.dialogmelding.DialogmeldingKodeverk.HENVENDELSE
import no.nav.helseidnavtest.dialogmelding.DialogmeldingType.DIALOG_NOTAT
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.DMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.HMOF
import no.nav.helseidnavtest.dialogmelding.ObjectFactories.VOF
import no.nav.helseidnavtest.dialogmelding.Pasient
import no.nav.helseidnavtest.edi20.EDI20DialogmeldingGenerator.Part
import no.nav.helseopplysninger.hodemelding.XMLCV
import no.nav.helseopplysninger.hodemelding.XMLMsgInfo
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.*

@Component
class EDI20DialogmeldingMapper {

    fun hodemelding(fra: Part, til: Part, pasient: Pasient, vedlegg: Pair<URI, String>?) =
        msgInfo(msgInfo(fra, til, pasient)).apply {
            vedlegg?.let { document.addAll(listOf(dialogmelding("Dialogmelding"), refDokument(it.first, it.second))) }
        }

    fun hodemelding(fra: Part, til: Part, pasient: Pasient, vedlegg: MultipartFile?) =
        msgInfo(msgInfo(fra, til, pasient)).apply {
            vedlegg?.let { document.addAll(listOf(dialogmelding("Dialogmelding"), inlineDokument(it.bytes))) }
        }

    private fun msgInfo(info: XMLMsgInfo) = HMOF.createXMLMsgHead().apply {
        msgInfo = info
    }

    private fun msgInfo(fra: Part, til: Part, pasient: Pasient) =
        HMOF.createXMLMsgInfo().apply {
            type = type()
            sender = avsender(fra)
            receiver = mottaker(til)
            patient = pasient(pasient)
        }

    private fun XMLMsgInfo.type() = HMOF.createXMLCS().apply {
        dn = "Notat"
        v = DIALOG_NOTAT.name
        miGversion = VERSION
        genDate = now()
        msgId = "${UUID.randomUUID()}"
    }

    private fun avsender(fra: Part) =
        HMOF.createXMLSender().apply {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = fra.navn.first
                ident.add(ident(NAV_HERID.verdi, type(NAV_OID, HER, HER_DESC)))
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = fra.navn.second
                    ident.add(ident(fra.id.verdi, type(NAV_OID, HER, HER_DESC)))
                }
            }
        }

    private fun mottaker(til: Part) =
        HMOF.createXMLReceiver().apply {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = til.navn.first
                ident.add(ident(NAV_HERID.verdi, type(NAV_OID, HER, HER_DESC)))
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = til.navn.second
                    ident.add(ident(til.id.verdi, type(NAV_OID, HER, HER_DESC)))
                }
            }
        }

    private fun pasient(pasient: Pasient) =
        HMOF.createXMLPatient().apply {
            with(pasient) {
                familyName = navn.etternavn
                navn.mellomnavn?.let { middleName = it }
                givenName = navn.fornavn
                ident.add(ident(id))
            }
        }

    private fun dialogmelding(tekst: String) =
        HMOF.createXMLDocument().apply {
            refDoc = HMOF.createXMLRefDoc().apply {
                issueDate = HMOF.createXMLTS().apply {
                    v = LocalDate.now().format(ISO_DATE)
                }
                msgType = HMOF.createXMLCS().apply {
                    dn = "XML-instans"
                    v = "XML"
                }
                content = HMOF.createXMLRefDocContent().apply {
                    any.add(DMOF.createXMLDialogmelding().apply {
                        notat.add(DMOF.createXMLNotat().apply {
                            tekstNotatInnhold = tekst
                            temaKodet = DMOF.createCV().apply {
                                s = HENVENDELSE.id
                                v = "${KODE8.value}"
                                dn = "Melding fra NAV"
                            }
                        })
                    })
                }
            }
        }

    private fun refDokument(uri: URI, contentType: String) =
        HMOF.createXMLDocument().apply {
            refDoc = HMOF.createXMLRefDoc().apply {
                issueDate = HMOF.createXMLTS().apply {
                    v = LocalDate.now().format(ISO_DATE)
                }
                msgType = HMOF.createXMLCS().apply {
                    dn = VEDLEGG
                    v = "A"
                }
                mimeType = contentType
                description = "Beskrivelse av vedlegg"
                fileReference = "$uri"
            }
        }

    private fun inlineDokument(vedlegg: ByteArray) =
        HMOF.createXMLDocument().apply {
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
                        value = vedlegg
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

    private fun ident(id: String, type: XMLCV) = HMOF.createXMLIdent().apply {
        this.id = id
        typeId = type
    }

    private fun ident(fnr: Fødselsnummer) = ident(fnr.verdi, type(HER_OID, fnr.type.name, fnr.type.verdi))

    companion object {
        private const val VEDLEGG = "Vedlegg"
        private const val VERSION = "v1.2 2006-05-24"
        private const val HER_DESC = "HER-id"
        private val NAV_HERID = HerId(90128)
        private const val NAV_OID = "2.16.578.1.12.4.1.1.9051"
        private const val HER_OID = "2.16.578.1.12.4.1.1.8116"
        private const val HER = "HER"
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
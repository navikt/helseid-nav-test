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
import no.nav.helseidnavtest.oppslag.adresse.Innsending
import no.nav.helseidnavtest.oppslag.adresse.Innsending.Parter
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart
import no.nav.helseidnavtest.oppslag.adresse.KommunikasjonsPart.*
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.hodemelding.*
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.TEXT_XML_VALUE
import org.springframework.stereotype.Component
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ISO_DATE
import java.util.*

@Component
class EDI20DialogmeldingMapper {

    val herIdType = type(NAV_OID, HER, HER_DESC)

    fun hodemelding(innsending: Innsending) =
        msgHead(msgInfo(innsending)).apply {
            innsending.vedlegg?.let {
                document.addAll(listOf(dialogmelding("Dialogmelding"), inlineDokument(it)))
            }
            innsending.ref?.let {
                document.addAll(listOf(dialogmelding("Dialogmelding"), refDokument(it.first, it.second)))
            }
        }

    private fun msgHead(info: XMLMsgInfo) = HMOF.createXMLMsgHead().apply {
        msgInfo = info
    }

    private fun msgInfo(innsending: Innsending) =
        HMOF.createXMLMsgInfo().apply {
            with(innsending) {
                type = type(innsending.id)
                sender = avsender(parter.avsender)
                receiver = mottaker(parter.mottaker)
                patient = pasient(pasient)
            }
        }

    private fun XMLMsgInfo.type(uuid: UUID) = HMOF.createXMLCS().apply {
        dn = "Notat"
        v = DIALOG_NOTAT.name
        miGversion = VERSION
        genDate = now()
        msgId = "$uuid"
    }

    private fun avsender(tjeneste: KommunikasjonsPart) =
        HMOF.createXMLSender().apply {
            tjeneste as Tjeneste
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = tjeneste.virksomhet.orgNavn
                ident.add(ident(tjeneste.virksomhet.herId, herIdType))
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = tjeneste.orgNavn
                    ident.add(ident(tjeneste.herId, herIdType))
                }
            }
        }

    private fun mottaker(mottaker: Mottaker) =
        HMOF.createXMLReceiver().apply {
            with(mottaker.part) {
                when (this) {
                    is Tjeneste -> {
                        organisation = HMOF.createXMLOrganisation().apply {
                            organisationName = virksomhet.orgNavn
                            ident.add(ident(virksomhet.herId, herIdType))
                            organisation = HMOF.createXMLOrganisation().apply {
                                organisationName = orgNavn
                                ident.add(ident(herId, herIdType))
                            }
                        }
                    }

                    is VirksomhetPerson -> {
                        organisation = HMOF.createXMLOrganisation().apply {
                            organisationName = virksomhet.orgNavn
                            ident.add(ident(virksomhet.herId, herIdType))
                            organisation = HMOF.createXMLOrganisation().apply {
                                organisationName = orgNavn
                                ident.add(ident(herId, herIdType))
                                healthcareProfessional = HMOF.createXMLHealthcareProfessional().apply {
                                    givenName = mottaker.navn.fornavn
                                    mottaker.navn.mellomnavn?.let { middleName = it }
                                    familyName = mottaker.navn.etternavn
                                }
                            }
                        }
                    }

                    else -> throw IllegalArgumentException("Ukjent mottaker $this")
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

    private fun ident(id: HerId, type: XMLCV) = ident(id.verdi, type)
    private fun ident(id: String, type: XMLCV) = HMOF.createXMLIdent().apply {
        this.id = id
        typeId = type
    }

    private fun ident(fnr: Fødselsnummer) = ident(fnr.verdi, type(HER_OID, fnr.type.name, fnr.type.verdi))

    companion object {
        private const val VEDLEGG = "Vedlegg"
        private const val VERSION = "v1.2 2006-05-24"
        private const val HER_DESC = "HER-id"
        private const val NAV_OID = "2.16.578.1.12.4.1.1.9051"
        private const val HER_OID = "2.16.578.1.12.4.1.1.8116"
        private const val HER = "HER"
    }

    final fun type(s: String, v: String, dn: String) =
        XMLCV().apply {
            this.s = s
            this.v = v
            this.dn = dn
        }

    fun innsending(hode: XMLMsgHead) =
        with(hode.msgInfo) {
            Innsending(UUID.fromString(hode.msgInfo.msgId), parter(sender, receiver), pasient(patient))
        }

    private fun parter(sender: XMLSender, mottaker: XMLReceiver): Parter =
        with(mottaker.organisation.healthcareProfessional) {
            Parter(partFra(sender), Mottaker(partFra(mottaker), Navn(givenName, middleName, familyName)))
        }

    private fun partFra(sender: XMLSender) =
        with(sender.organisation) {
            Tjeneste(aktiv = true,
                visningsNavn = organisation.organisationName,
                herId = organisation.ident.herId(),
                navn = organisation.organisationName,
                virksomhet = Virksomhet(aktiv = true,
                    herId = ident.herId(),
                    navn = organisationName,
                    visningsNavn = organisationName))
        }

    private fun List<XMLIdent>.herId() = HerId(first().id)

    private fun partFra(mottaker: XMLReceiver) =
        with(mottaker.organisation) {
            Tjeneste(aktiv = true,
                visningsNavn = organisation.organisationName,
                herId = organisation.ident.herId(),
                navn = organisation.organisationName,
                virksomhet = Virksomhet(aktiv = true,
                    herId = ident.herId(),
                    navn = organisationName,
                    visningsNavn = organisationName))
        }

    private fun pasient(pasient: XMLPatient) =
        with(pasient) {
            Pasient(Fødselsnummer(ident.first().id),
                Navn(givenName, middleName, familyName))
        }

    fun apprec(apprec: XMLAppRec) = apprec
    //return Apprec(result = Apprec.ApprecResult.OK) // TODO
}
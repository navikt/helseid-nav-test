package no.nav.helseidnavtest.util

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Unmarshaller
import no.nav.helseidnavtest.domain.*
import no.nav.helseidnavtest.domain.DialogmeldingKodeverk.*
import no.nav.helseidnavtest.domain.DialogmeldingType.*
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLNotat
import no.nav.helseopplysninger.fellesformat2.XMLMottakenhetBlokk
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.*
import java.math.BigInteger.*
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream
import javax.xml.datatype.DatatypeConstants.*
import javax.xml.datatype.DatatypeFactory

val apprecJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java)

val apprecUnmarshaller: Unmarshaller = apprecJaxBContext.createUnmarshaller().apply {

    // TODO setAdapter(XMLDateTimeAdapter::class.java, XMLDateTimeAdapter())
   // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
}


   val FFOF = no.nav.helseopplysninger.fellesformat2.ObjectFactory()
   val HMOF =  no.nav.helseopplysninger.hodemelding.ObjectFactory()
object JAXB{
    fun createFellesformat(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker) =
        FFOF.createXMLEIFellesformat().apply {
            any.add(createMsgHead(melding, arbeidstaker))
            any.add(createMottakenhetBlokk(melding))
            any.add(createSporinformasjonBlokk())
        }
}

fun createMsgHead(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker) =
    HMOF.createXMLMsgHead().apply {
        msgInfo = createMsgInfo(melding, arbeidstaker)
        // TODO document.add(createDialogmeldingDocument(melding))
        // TODO document.add(createVedleggDocument(melding))
}
fun createMottakenhetBlokk(melding: DialogmeldingToBehandlerBestilling) =
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

fun createSporinformasjonBlokk(): JAXBElement<XMLSporinformasjonBlokkType> {
    val xmlSporinformasjonBlokkType = FFOF.createXMLSporinformasjonBlokkType().apply {
        programID = "Helseopplysninger webapp"
        programVersjonID = "1.0"
        programResultatKoder = ZERO
        tidsstempel = DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar()).apply {
            millisecond = FIELD_UNDEFINED
            timezone = FIELD_UNDEFINED
        }
    }
    return FFOF.createSporinformasjonBlokk(xmlSporinformasjonBlokkType)
}

fun createMsgInfo(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker): XMLMsgInfo {
    return HMOF.createXMLMsgInfo().apply {
        type = HMOF.createXMLCS().apply {
            dn = "Notat"
            v = DIALOG_NOTAT.name
            miGversion = "v1.2 2006-05-24"
            genDate = LocalDateTime.now()
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
fun createReceiver(bestiling: DialogmeldingToBehandlerBestilling) =
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
                        ident.add(HMOF.createXMLIdentForPersonident(it))
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
            ident.add(HMOF.createXMLIdentForPersonident(arbeidstakerPersonident))
        }
    }
fun no.nav.helseopplysninger.hodemelding.ObjectFactory.createXMLIdentForPersonident(personident: Personident): XMLIdent {
    val personidentIsDNR = personident.isDNR()
    return this.createXMLIdent().apply {
        id = personident.value
        typeId = createXMLCV().apply {
            dn = if (personidentIsDNR) "D-nummer" else "Fødselsnummer"
            s = "2.16.578.1.12.4.1.1.8116"
            v = if (personidentIsDNR) "DNR" else "FNR"
        }
    }
}


class Fellesformat private constructor(val eIFellesformat: XMLEIFellesformat) {
    var message: String? = null
        private set
    private val mottakenhetBlokkListe: MutableList<XMLMottakenhetBlokk>
    private val hodemeldingListe: MutableList<Hodemelding>

    constructor(fellesformat: XMLEIFellesformat, marshaller: Function<XMLEIFellesformat?, String?>) : this(fellesformat) {
        message = marshaller.apply(fellesformat)
    }

    init {
        mottakenhetBlokkListe = ArrayList()
        hodemeldingListe = ArrayList()

        eIFellesformat.any.forEach(
            Consumer { melding: Any? ->
                if (melding is XMLMsgHead) {
                    hodemeldingListe.add(Hodemelding(melding))
                } else if (melding is XMLMottakenhetBlokk) {
                    mottakenhetBlokkListe.add(melding)
                }
            }
        )
    }
}
class Hodemelding(private val msgHead: XMLMsgHead,private val dokumentListe: MutableList<Dokument> = mutableListOf()) {

    private val xMLDocumentStream: Stream<XMLDocument>
        get() = Stream.of(msgHead)
            .map { xmlMsgHead: XMLMsgHead -> xmlMsgHead.document }
            .flatMap { obj: List<XMLDocument?> -> obj.stream() }

    fun harVedlegg() = dokumentListe.any(Dokument::harVedlegg)

    val messageId: String
        get() = msgHead.msgInfo.msgId
     val dokIdNotatStream: Stream<String?>
        get() = dokumentListe.stream().flatMap(Dokument::dokIdNotatStream)

    init {
        xMLDocumentStream.map(::Dokument)
            .forEach(dokumentListe::add)
    }
}

class Dokument(xmlDocument: XMLDocument) {
    private val dialogmeldingListe: MutableList<Dialogmelding>
    private val vedleggListe: MutableList<Vedlegg>
    private fun getContent(xmlDocument: XMLDocument): Stream<Any> {
        return Stream.of(xmlDocument)
            .map { it.refDoc }
            .map { it.content }
            .map(XMLRefDoc.Content::getAny)
            .flatMap(List<Any>::stream)
    }


    fun harVedlegg(): Boolean {
        return vedleggListe.isNotEmpty()
    }

   val dokIdNotatStream: Stream<String?>
        get() = dialogmeldingListe.stream().flatMap(Dialogmelding::dokIdNotatStream)

    init {
        dialogmeldingListe = ArrayList()
        vedleggListe = ArrayList()
        getContent(xmlDocument).forEach { o: Any? ->
            when (o) {
                is XMLDialogmelding -> dialogmeldingListe.add(Dialogmelding1_0(o))
                is XMLBase64Container -> vedleggListe.add(Vedlegg(o))
            }
        }
    }
}

class Dialogmelding1_0(dialogmelding1_0: XMLDialogmelding) : DialogmeldingAbstract() {
    override fun versjon(): Dialogmelding.Versjon {
        return Dialogmelding.Versjon._1_0
    }

    init {
        dialogmelding1_0.notat.stream().map(Function { Notat1_0() }).forEach(notatListe::add)
    }
}


class Notat1_0 : Notat {
    private val xmlNotat: XMLNotat? = null
    override val dokIdNotat: String?
        get() = xmlNotat!!.dokIdNotat
}



interface Dialogmelding {
    @Suppress("ktlint")
    enum class Versjon { _1_0}

    fun versjon(): Versjon?
    val dokIdNotatStream: Stream<String?>?
}
class Vedlegg(private val container: XMLBase64Container? = null)

abstract class DialogmeldingAbstract internal constructor() : Dialogmelding {
    var notatListe: MutableList<Notat>

    override val dokIdNotatStream: Stream<String?>?
        get() = notatListe.stream().map { obj: Notat -> obj.dokIdNotat }

    init {
        notatListe = ArrayList()
    }
}


interface Notat {
    val dokIdNotat: String?
}




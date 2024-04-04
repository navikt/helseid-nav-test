package no.nav.helseidnavtest.util

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller.*
import jakarta.xml.bind.Unmarshaller
import no.nav.helseidnavtest.util.DialogmeldingType.*
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLForesporsel
import no.nav.helseopplysninger.dialogmelding.XMLNotat
import no.nav.helseopplysninger.fellesformat2.ObjectFactory
import no.nav.helseopplysninger.fellesformat2.XMLMottakenhetBlokk
import no.nav.helseopplysninger.hodemelding.*
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream
import javax.xml.stream.XMLStreamReader
import javax.xml.transform.stream.StreamResult

val apprecJaxBContext: JAXBContext = JAXBContext.newInstance(XMLEIFellesformat::class.java, XMLAppRec::class.java)

val apprecUnmarshaller: Unmarshaller = apprecJaxBContext.createUnmarshaller().apply {
   // TODO setAdapter(LocalDateTimeXmlAdapter::class.java, XMLDateTimeAdapter())
   // TODO setAdapter(LocalDateXmlAdapter::class.java, XMLDateAdapter())
}

private fun opprettDialogmelding(
    melding: DialogmeldingToBehandlerBestilling,
    arbeidstaker: Arbeidstaker,
): Fellesformat {
    val xmleiFellesformat = createFellesformat(
        melding = melding,
        arbeidstaker = arbeidstaker,
    )
    return Fellesformat(xmleiFellesformat, JAXB::marshallDialogmelding1_0)
}
object JAXB {
    private var DIALOGMELDING_CONTEXT_1_0: JAXBContext? = null

    fun marshallDialogmelding1_0(element: Any?): String {
        return try {
            val writer = StringWriter()
            val marshaller = DIALOGMELDING_CONTEXT_1_0!!.createMarshaller()
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true)
            marshaller.setProperty(JAXB_ENCODING, "UTF-8")
            marshaller.marshal(element, StreamResult(writer))
            writer.toString()
        } catch (e: JAXBException) {
            throw RuntimeException(e)
        }
    }

    inline fun <reified T> unmarshallObject(xmlStreamReader: XMLStreamReader): T {
        val jaxbContext: JAXBContext = JAXBContext.newInstance(T::class.java)
        val unmarshaller: Unmarshaller = jaxbContext.createUnmarshaller()

        return unmarshaller.unmarshal(xmlStreamReader) as T
    }

    init {
        try {
            DIALOGMELDING_CONTEXT_1_0 = JAXBContext.newInstance(
                XMLEIFellesformat::class.java,
                XMLMsgHead::class.java,
                XMLDialogmelding::class.java,
                XMLBase64Container::class.java,
                XMLSporinformasjonBlokkType::class.java,
            )
        } catch (e: JAXBException) {
            throw RuntimeException(e)
        }
    }
}

fun createFellesformat(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker) =
     ObjectFactory().createXMLEIFellesformat().apply {
        any.add(createMsgHead(melding, arbeidstaker))
       // TODO  any.add(createMottakenhetBlokk(melding))
        // TODO any.add(createSporinformasjonBlokk())
    }

fun createMsgHead(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker) =
    no.nav.helseopplysninger.hodemelding.ObjectFactory().createXMLMsgHead().apply {
        msgInfo = createMsgInfo(melding, arbeidstaker)
        // TODO document.add(createDialogmeldingDocument(melding))
        // TODO document.add(createVedleggDocument(melding))
}

data class Arbeidstaker(
    val arbeidstakerPersonident: Personident,
    val fornavn: String = "",
    val mellomnavn: String? = null,
    val etternavn: String = "",
    val mottatt: OffsetDateTime,
)
data class Personident(val value: String) {
    init {
        if (!elevenDigits.matches(value)) {
            throw IllegalArgumentException("Value is not a valid Personident")
        }
    }
}

val elevenDigits = Regex("^\\d{11}\$")

data class DialogmeldingToBehandlerBestilling(
    val uuid: UUID,
    val behandler: Behandler,
    val arbeidstakerPersonident: Personident,
    val parentRef: String?,
    val conversationUuid: UUID,
    val type: DialogmeldingType,
    val kodeverk: DialogmeldingKodeverk?, // må tillate null her siden persisterte bestillinger kan mangle denne verdien
    val kode: DialogmeldingKode,
    val tekst: String?,
    val vedlegg: ByteArray? = null,
)

enum class DialogmeldingKode(
    val value: Int
) {
    KODE1(1),
    KODE2(2),
    KODE3(3),
    KODE4(4),
    KODE8(8),
    KODE9(9);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
enum class DialogmeldingKodeverk(
    val kodeverkId: String,
) {
    DIALOGMOTE("2.16.578.1.12.4.1.1.8125"),
    HENVENDELSE("2.16.578.1.12.4.1.1.8127"),
    FORESPORSEL("2.16.578.1.12.4.1.1.8129"),
}

enum class DialogmeldingType() {
    DIALOG_FORESPORSEL,
    DIALOG_NOTAT,
    OPPFOLGINGSPLAN,
}

fun Personident.isDNR() = this.value[0].digitToInt() > 3

data class Behandler(
    val behandlerRef: UUID,
    val personident: Personident?,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val herId: Int?,
    val hprId: Int?,
    val telefon: String?,
    val kontor: BehandlerKontor,
    val kategori: BehandlerKategori,
    val mottatt: OffsetDateTime,
    val invalidated: OffsetDateTime? = null,
    val suspendert: Boolean,
)

data class BehandlerKontor(
    val partnerId: PartnerId,
    val herId: Int?,
    val navn: String?,
    val adresse: String?,
    val postnummer: String?,
    val poststed: String?,
    val orgnummer: Virksomhetsnummer?,
    val dialogmeldingEnabled: Boolean,
    val dialogmeldingEnabledLocked: Boolean,
    val system: String?,
    val mottatt: OffsetDateTime,
)

enum class BehandlerKategori(
    val kategoriKode: String,
) {
    FYSIOTERAPEUT("FT"),
    KIROPRAKTOR("KI"),
    LEGE("LE"),
    MANUELLTERAPEUT("MT"),
    TANNLEGE("TL");

    companion object {
        fun fromKategoriKode(kategori: String?): BehandlerKategori? =
            values().firstOrNull { it.kategoriKode == kategori }
    }
}
data class PartnerId(val value: Int) {
    override fun toString(): String {
        return value.toString()
    }
}
data class Virksomhetsnummer(val value: String) {
    private val nineDigits = Regex("^\\d{9}\$")

    init {
        if (!nineDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid Virksomhetsnummer")
        }
    }
}

fun createMsgInfo(melding: DialogmeldingToBehandlerBestilling, arbeidstaker: Arbeidstaker): XMLMsgInfo {
    val factory = no.nav.helseopplysninger.hodemelding.ObjectFactory()
    return factory.createXMLMsgInfo().apply {
        type = factory.createXMLCS().apply {
            dn = if (
                melding.type == DIALOG_NOTAT ||
                melding.type == OPPFOLGINGSPLAN
            ) "Notat" else "Forespørsel"
            v = if (
                melding.type == DIALOG_NOTAT ||
                melding.type == OPPFOLGINGSPLAN
            ) DIALOG_NOTAT.name else DIALOG_FORESPORSEL.name
            miGversion = "v1.2 2006-05-24"
            genDate = LocalDateTime.now()
            msgId = melding.uuid.toString()
            ack = factory.createXMLCS().apply {
                dn = "Ja"
                v = "J"
            }.also {
                if (melding.type != OPPFOLGINGSPLAN) {
                    conversationRef = factory.createXMLConversationRef().apply {
                        refToConversation = melding.conversationUuid.toString()
                        refToParent = melding.parentRef ?: melding.conversationUuid.toString()
                    }
                }
                sender = createSender()
                receiver = createReceiver(melding)
                patient = createPasient(arbeidstaker)
            }
        }
    }
}

fun createSender(): XMLSender {
    val factory = no.nav.helseopplysninger.hodemelding.ObjectFactory()
    return factory.createXMLSender().apply {
        organisation = factory.createXMLOrganisation().apply {
            organisationName = "NAV"
            ident.add(
                factory.createXMLIdent().apply {
                    id = "889640782"
                    typeId = factory.createXMLCV().apply {
                        dn = "Organisasjonsnummeret i Enhetsregisteret"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "ENH"
                    }
                })
            ident.add(
                factory.createXMLIdent().apply {
                    id = "79768"
                    typeId = factory.createXMLCV().apply {
                        dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "HER"
                    }
                })
        }
    }
}
fun createReceiver(melding: DialogmeldingToBehandlerBestilling): XMLReceiver {
    val factory = no.nav.helseopplysninger.hodemelding.ObjectFactory()
    return factory.createXMLReceiver().apply {
        organisation = factory.createXMLOrganisation().apply {
            organisationName = melding.behandler.kontor.navn
            ident.add(
                factory.createXMLIdent().apply {
                    id = melding.behandler.kontor.herId.toString()
                    typeId = factory.createXMLCV().apply {
                        dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "HER"
                    }
                })
            if (melding.behandler.kontor.orgnummer != null) {
                ident.add(
                    factory.createXMLIdent().apply {
                        id = melding.behandler.kontor.orgnummer.value
                        typeId = factory.createXMLCV().apply {
                            dn = "Organisasjonsnummeret i Enhetsregisteret"
                            s = "2.16.578.1.12.4.1.1.9051"
                            v = "ENH"
                        }})
            }
            address = factory.createXMLAddress().apply {
                type = factory.createXMLCS().apply {
                    dn = "Besøksadresse"
                    v = "RES"
                }
                streetAdr = melding.behandler.kontor.adresse
                postalCode = melding.behandler.kontor.postnummer
                city = melding.behandler.kontor.poststed
            }
            healthcareProfessional = factory.createXMLHealthcareProfessional().apply {
                if (melding.type == OPPFOLGINGSPLAN) {
                    roleToPatient = factory.createXMLCV().apply {
                        v = "6"
                        s = "2.16.578.1.12.4.1.1.9034"
                        dn = "Fastlege"
                    }
                }
                familyName = melding.behandler.etternavn
                middleName = melding.behandler.mellomnavn
                givenName = melding.behandler.fornavn
                melding.behandler.personident?.let {
                    ident.add(factory.createXMLIdentForPersonident(it))
                }
                if (melding.behandler.hprId != null) {
                    ident.add(
                        factory.createXMLIdent().apply {
                            id = melding.behandler.hprId.toString()
                            typeId = factory.createXMLCV().apply {
                                dn = "HPR-nummer"
                                s = "2.16.578.1.12.4.1.1.8116"
                                v = "HPR"
                            }
                        })
                }
                if (melding.behandler.herId != null) {
                    ident.add(
                        factory.createXMLIdent().apply {
                            id = melding.behandler.herId.toString()
                            typeId = factory.createXMLCV().apply {
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

fun createPasient(arbeidstaker: Arbeidstaker): XMLPatient {
    val factory = no.nav.helseopplysninger.hodemelding.ObjectFactory()
    return factory.createXMLPatient().apply {
        familyName = arbeidstaker.etternavn
        middleName = arbeidstaker.mellomnavn
        givenName = arbeidstaker.fornavn
        ident.add(
            factory.createXMLIdentForPersonident(arbeidstaker.arbeidstakerPersonident)
        )
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
class Hodemelding(msgHead: XMLMsgHead) {
    private val msgHead: XMLMsgHead
    private val dokumentListe: MutableList<Dokument>
    private val xMLDocumentStream: Stream<XMLDocument>
        get() = Stream.of(msgHead)
            .map { xmlMsgHead: XMLMsgHead -> xmlMsgHead.document }
            .flatMap { obj: List<XMLDocument?> -> obj.stream() }

    fun erForesporsel(): Boolean {
        return dokumentListe.stream().anyMatch { obj: Dokument -> obj.erForesporsel() }
    }

    fun erNotat(): Boolean {
        return dokumentListe.stream().anyMatch { obj: Dokument -> obj.erNotat() }
    }

    fun harVedlegg(): Boolean {
        return dokumentListe.stream().anyMatch { obj: Dokument -> obj.harVedlegg() }
    }

    val messageId: String
        get() = msgHead.msgInfo.msgId
    val dokIdForesporselStream: Stream<String?>
        get() = dokumentListe.stream().flatMap { obj: Dokument -> obj.dokIdForesporselStream }
    val dokIdNotatStream: Stream<String?>
        get() = dokumentListe.stream().flatMap { obj: Dokument -> obj.dokIdNotatStream }

    init {
        this.msgHead = msgHead
        dokumentListe = ArrayList()
        xMLDocumentStream.map { xmlDocument: XMLDocument -> Dokument(xmlDocument) }
            .forEach { dokument -> dokumentListe.add(dokument) }
    }
}

class Dokument(xmlDocument: XMLDocument) {
    private val dialogmeldingListe: MutableList<Dialogmelding>
    private val vedleggListe: MutableList<Vedlegg>
    private fun getContent(xmlDocument: XMLDocument): Stream<Any> {
        return Stream.of(xmlDocument)
            .map { obj: XMLDocument -> obj.refDoc }
            .map { obj: XMLRefDoc -> obj.content }
            .map { obj: XMLRefDoc.Content -> obj.getAny() }
            .flatMap { obj: List<Any> -> obj.stream() }
    }

    fun erForesporsel(): Boolean {
        return dialogmeldingListe.stream().anyMatch { obj: Dialogmelding -> obj.erForesporsel() }
    }

    fun erNotat(): Boolean {
        return dialogmeldingListe.stream().anyMatch { obj: Dialogmelding -> obj.erNotat() }
    }

    fun harVedlegg(): Boolean {
        return !vedleggListe.isEmpty()
    }

    val dokIdForesporselStream: Stream<String?>
        get() = dialogmeldingListe.stream().flatMap { obj: Dialogmelding -> obj.dokIdForesporselStream }
    val dokIdNotatStream: Stream<String?>
        get() = dialogmeldingListe.stream().flatMap { obj: Dialogmelding -> obj.dokIdNotatStream }

    init {
        dialogmeldingListe = ArrayList()
        vedleggListe = ArrayList()
        getContent(xmlDocument).forEach { o: Any? ->
            if (o is XMLDialogmelding) {
                dialogmeldingListe.add(Dialogmelding1_0(o))
            }// TODO  else if (o is no.kith.xmlstds.dialog._2013_01_23.XMLDialogmelding) {
              // TODO   dialogmeldingListe.add(Dialogmelding1_1(o))
         // TODO  }
        else if (o is XMLBase64Container) {
                vedleggListe.add(Vedlegg(o as XMLBase64Container?))
            }
        }
    }
}

class Notat1_1 : Notat {
    private val xmlNotat: XMLNotat? = null
    override val dokIdNotat: String?
        get() = xmlNotat!!.dokIdNotat
}

class Dialogmelding1_0(dialogmelding1_0: XMLDialogmelding) : DialogmeldingAbstract() {
    override fun versjon(): Dialogmelding.Versjon? {
        return Dialogmelding.Versjon._1_0
    }

    init {
        dialogmelding1_0.notat.stream().map(Function { Notat1_0() }).forEach(notatListe::add)
        dialogmelding1_0.foresporsel.stream().map(Function { Foresporsel1_0() }).forEach(foresporselListe::add)
    }
}

class Foresporsel1_0 : Foresporsel {
    private val xmlForesporsel: XMLForesporsel? = null
    override val dokIdForesporsel: String?
        get() = xmlForesporsel!!.dokIdForesp
}

class Notat1_0 : Notat {
    private val xmlNotat: XMLNotat? = null
    override val dokIdNotat: String?
        get() = xmlNotat!!.dokIdNotat
}

class Dialogmelding1_1(dialogmelding1_1: XMLDialogmelding) : DialogmeldingAbstract() {
    override fun versjon(): Dialogmelding.Versjon? {
        return Dialogmelding.Versjon._1_1
    }

    init {
        dialogmelding1_1.notat.stream().map(Function { Notat1_1() }).forEach(notatListe::add)
        dialogmelding1_1.foresporsel.stream().map(Function { Foresporsel1_1() }).forEach(foresporselListe::add)
    }
}

class Foresporsel1_1 : Foresporsel {
    private val xmlForesporsel: XMLForesporsel? = null
    override val dokIdForesporsel: String?
        get() = xmlForesporsel!!.dokIdForesp
}

interface Dialogmelding {
    @Suppress("ktlint")
    enum class Versjon {
        _1_0, _1_1
    }

    fun versjon(): Versjon?
    fun erForesporsel(): Boolean
    fun erNotat(): Boolean
    val dokIdForesporselStream: Stream<String?>?
    val dokIdNotatStream: Stream<String?>?
}
class Vedlegg(private val container: XMLBase64Container? = null)

abstract class DialogmeldingAbstract internal constructor() : Dialogmelding {
    var notatListe: MutableList<Notat>
    var foresporselListe: MutableList<Foresporsel>
    override fun erForesporsel(): Boolean {
        return !foresporselListe.isEmpty()
    }

    override fun erNotat(): Boolean {
        return !notatListe.isEmpty()
    }

    override val dokIdForesporselStream: Stream<String?>?
        get() = foresporselListe.stream().map { obj: Foresporsel -> obj.dokIdForesporsel }
    override val dokIdNotatStream: Stream<String?>?
        get() = notatListe.stream().map { obj: Notat -> obj.dokIdNotat }

    init {
        notatListe = ArrayList()
        foresporselListe = ArrayList()
    }
}

interface Foresporsel {
    val dokIdForesporsel: String?
}

interface Notat {
    val dokIdNotat: String?
}




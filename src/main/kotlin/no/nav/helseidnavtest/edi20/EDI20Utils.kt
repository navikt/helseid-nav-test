package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.HERID
import org.springframework.http.HttpHeaders
import java.net.URI
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

fun HerId.other() = HerId.of(verdi.other())
fun String.other() =
    when (this) {
        EDI1_ID -> EDI2_ID
        EDI2_ID -> EDI1_ID
        else -> throw IllegalArgumentException("Ikke støttet herId $this")
    }

@Target(VALUE_PARAMETER)
@Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID], description = "ID for EDI1 eller EDI2"))
annotation class Herid

fun HttpHeaders.herId(herId: HerId) = herId(herId.verdi)
fun HttpHeaders.herId(herId: String) = add(HERID, herId)

fun URI.key() = path.substringAfterLast('/')

package no.nav.helseidnavtest.edi20

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI1_ID
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI2_ID
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

object EDI20Utils {
     fun String.other() =
        when(this) {
            EDI1_ID ->  EDI2_ID
            EDI2_ID ->  EDI1_ID
            else -> throw IllegalArgumentException("Ikke støttet herId $this")
        }
}
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@Parameter(schema = Schema(allowableValues = [EDI1_ID, EDI2_ID]))
annotation class Herid
package no.nav.helseidnavtest.edi20
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.helseidnavtest.dialogmelding.Fødselsnummer
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController(EDI20)
class EDI20Controller(private val a: EDI20Service) {

    private val log = getLogger(EDI20Controller::class.java)

    @GetMapping("/messages") fun messages(@Parameter(schema = Schema(implementation = HerIds::class)) @RequestParam herId: HerId) = a.poll(herId)

    @GetMapping("/dialogmelding") fun dialogmelding(@RequestParam pasient: Fødselsnummer): String {
        runCatching {
            a.send(pasient)
            return "OK"
        }.getOrElse {
            log.error("Feil ved generering av dialogmelding", it)
            return "NOT OK"
        }
    }
    @Schema(name = "HerId", description = "HerId for sender and mottager")
    enum class HerIds(val herId: HerId) {
        SENDER_19(EDI20Config.SENDER.first),
        MOTTAGER_20(EDI20Config.MOTTAGER.first)
    }
}

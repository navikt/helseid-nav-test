package no.nav.helseidnavtest.oppslag.adresse

import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.oppslag.AbstractCXFAdapter
import no.nhn.register.communicationparty.CommunicationParty
import no.nhn.register.communicationparty.ICommunicationPartyService
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage as CommPartyFault

@Component
class AdresseRegisterCXFAdapter(cfg: AdresseRegisterConfig) : AbstractCXFAdapter(cfg) {

    private val client = client<ICommunicationPartyService>()

    fun herIdForId(id: String): Int = party(id).herId

    fun parent(herId: HerId) = details(herId).parentHerId
    
    fun nameForId(herId: HerId) = details(herId).name.value

    fun details(id: HerId) = client.getCommunicationPartyDetails(id.verdi.toInt())

    private fun party(id: String): CommunicationParty = runCatching {
        client.searchById(id).communicationParty.single()
    }.getOrElse {
        when (it) {
            is CommPartyFault -> throw NotFoundException(it.message, cfg.url, cause = it)
            is IllegalArgumentException -> throw IrrecoverableException(BAD_REQUEST, cfg.url, it.message, cause = it)
            is NoSuchElementException -> throw NotFoundException("Fant ikke kommunikasjonspart for $id",
                cfg.url,
                cause = it)

            is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,
                cfg.url,
                it.message,
                cause = it)

            else -> throw RecoverableException(BAD_REQUEST, cfg.url, it.message, it)
        }
    }

    override fun ping() = mapOf(Pair("ping", client.ping()))

}




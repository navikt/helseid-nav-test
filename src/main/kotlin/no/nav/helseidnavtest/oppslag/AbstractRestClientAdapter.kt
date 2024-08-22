package no.nav.helseidnavtest.oppslag

import com.nimbusds.oauth2.sdk.token.AccessTokenType
import com.nimbusds.oauth2.sdk.token.AccessTokenType.BEARER
import no.nav.helseidnavtest.dialogmelding.HerId
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_1
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_2
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.HERID
import no.nav.helseidnavtest.error.IrrecoverableException
import no.nav.helseidnavtest.error.IrrecoverableException.NotFoundException
import no.nav.helseidnavtest.error.RecoverableException
import no.nav.helseidnavtest.health.Pingable
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterConfig
import no.nhn.register.communicationparty.ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest.withClientRegistrationId
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.*

abstract class AbstractRestClientAdapter(
    protected open val restClient: RestClient, protected val cfg: AbstractRestConfig,
    private val pingClient: RestClient = restClient
) : Pingable {

    override fun ping(): Map<String, String> {
        if (isEnabled()) {
            pingClient
                .get()
                .uri(pingEndpoint())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful) { _, _ ->
                    log.trace("Ping ${pingEndpoint()} OK")
                }
            return emptyMap()
        } else return emptyMap()
    }

    override fun name() = cfg.name
    protected val baseUri = cfg.baseUri

    override fun pingEndpoint() = "${cfg.pingEndpoint}"
    override fun isEnabled() = cfg.isEnabled
    override fun toString() = "webClient=$restClient, cfg=$cfg, pingClient=$pingClient, baseUri=$baseUri"

    companion object {
        val log = getLogger(AbstractRestClientAdapter::class.java)

        private fun generellRequestInterceptor(key: String, value: () -> String) =
            ClientHttpRequestInterceptor { req, b, next ->
                req.headers.add(key, value())
                next.execute(req, b)
            }

        fun consumerRequestInterceptor() = generellRequestInterceptor(NAV_CONSUMER_ID) { HELSE }
        fun behandlingRequestInterceptor() = generellRequestInterceptor(BEHANDLINGSNUMMER) { BID }
        fun temaRequestInterceptor(tema: String) = generellRequestInterceptor(TEMA) { tema }

        private object CallIdGenerator {

            fun create() = "${UUID.randomUUID()}"
        }

        fun correlatingRequestInterceptor(defaultConsumerId: String) =
            ClientHttpRequestInterceptor { req, b, next ->
                with(req.headers) {
                    mapOf(
                        NAV_CONSUMER_ID to consumerId(defaultConsumerId),
                        NAV_CONSUMER_ID2 to consumerId(defaultConsumerId),
                        NAV_CALL_ID to callId(),
                        NAV_CALL_ID1 to callId(),
                        NAV_CALL_ID2 to callId(),
                        NAV_CALL_ID3 to callId()
                    ).forEach { (key, value) -> add(key, value) }
                }
                next.execute(req, b)
            }

        const val TEMA = "tema"
        const val HELSE = "helseopplysninger"
        private const val BEHANDLINGSNUMMER = "behandlingsnummer"
        private const val BID = "B287"
        private const val NAV_CONSUMER_ID = "Nav-Consumer-Id"
        private const val NAV_CONSUMER_ID2 = "consumerId"
        private const val NAV_CALL_ID = "Nav-CallId"
        private const val NAV_CALL_ID1 = "Nav-Call-Id"
        private const val NAV_CALL_ID2 = "callId"
        private const val NAV_CALL_ID3 = "X-Correlation-ID"

        private fun callId() = MDC.get(NAV_CALL_ID) ?: run {
            val id = CallIdGenerator.create()
            toMDC(NAV_CALL_ID, id)
            id
        }

        private fun consumerId(defaultValue: String): String = MDC.get(NAV_CONSUMER_ID) ?: run {
            toMDC(NAV_CONSUMER_ID, defaultValue)
            defaultValue
        }

        private fun toMDC(key: String, value: String?, defaultValue: String? = null) =
            MDC.put(key, value ?: defaultValue)
    }
}

open class TokenExchangingRequestInterceptor(private val clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager,
                                             private val tokenType: AccessTokenType = BEARER,
                                             defaultShortName: String? = null) : ClientHttpRequestInterceptor {

    private val mapper = HerIdToShortNameMapper(defaultShortName)
    protected val log = getLogger(TokenExchangingRequestInterceptor::class.java)

    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution) =
        with(req) {
            authorize(this)
            execution.execute(this, body)
        }

    protected fun authorize(req: HttpRequest) =
        clientManager.authorize(
            withClientRegistrationId(mapper.map(req))
                .principal("whatever")
                .build()
        )?.apply { ->
            req.headers.set(AUTHORIZATION, "${tokenType.value} ${accessToken.tokenValue}")
                .also {
                    log.info("Token {} exchanged for type {} og id {}",
                        accessToken.tokenValue,
                        tokenType.value,
                        clientRegistration.registrationId)
                }
        }

    private class HerIdToShortNameMapper(private val defaultShortName: String?) {
        private val log = getLogger(HerIdToShortNameMapper::class.java)

        fun map(req: HttpRequest) = defaultShortName ?: shortNameFromHeader(req)

        private fun shortNameFromHeader(req: HttpRequest) =
            when (req.headers[HERID]?.single()) {
                EDI_1.first.verdi -> EDI_1.second
                EDI_2.first.verdi -> EDI_2.second
                else -> throw IllegalArgumentException("No/unknown herId in request header")
            }.also {
                log.info("Mapped to shortName $it from header")
            }
    }
}

@Component
class CXFErrorHandler(private val cfg: AdresseRegisterConfig) {
    private val log = getLogger(CXFErrorHandler::class.java)
    fun handleError(it: Throwable, id: HerId): Nothing =
        when (it) {
            is ICommunicationPartyServiceGetCommunicationPartyDetailsGenericFaultFaultFaultMessage -> throw NotFoundException(
                it.message,
                cfg.url,
                cause = it)

            is IllegalArgumentException -> throw IrrecoverableException(BAD_REQUEST,
                cfg.url,
                it.message,
                cause = it)

            is NoSuchElementException -> throw NotFoundException("Fant ikke kommunikasjonspart for $id",
                cfg.url,
                cause = it)

            is IllegalStateException -> throw IrrecoverableException(INTERNAL_SERVER_ERROR,
                cfg.url,
                it.message,
                cause = it)

            else -> throw RecoverableException(BAD_REQUEST, cfg.url, it.message, it).also {
                log.warn(it.message, it)
            }
        }
}
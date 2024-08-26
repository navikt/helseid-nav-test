package no.nav.helseidnavtest.edi20

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.KeyUse.SIGNATURE
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.oauth2.sdk.token.AccessTokenType.DPOP
import no.nav.helseidnavtest.edi20.BestillingConfig.Companion.BESTILLING
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI20
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.PLAIN
import no.nav.helseidnavtest.edi20.EDI20DeftConfig.Companion.EDI20DEFT
import no.nav.helseidnavtest.oppslag.AbstractRestConfig
import no.nav.helseidnavtest.oppslag.TokenExchangingRequestInterceptor
import no.nav.helseidnavtest.oppslag.adresse.Bestilling
import no.nav.helseidnavtest.security.DPoPProofGenerator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD
import org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient.Builder
import java.util.*

@Configuration(proxyBeanMethods = true)
class EDI20BeanConfig {

    @Bean(BESTILLING)
    fun bestillingListenerContainerFactory(p: KafkaProperties) =
        ConcurrentKafkaListenerContainerFactory<UUID, Bestilling>().apply {
            containerProperties.isObservationEnabled = true
            containerProperties.ackMode = RECORD
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties(null).apply {
                put(TRUSTED_PACKAGES, "no.nav.helseidnavtest.oppslag.adresse")
            })
        }

    @Bean
    fun cacheManager() = ConcurrentMapCacheManager()

    @Bean
    @Qualifier(PLAIN)
    fun plainEdi20RestClient(b: Builder, cfg: EDI20Config) =
        b.baseUrl("${cfg.baseUri}")
            .messageConverters {
                it.addAll(listOf(FormHttpMessageConverter(),
                    OAuth2AccessTokenResponseHttpMessageConverter()))
            }
            .build()

    @Bean
    @Qualifier(EDI20)
    fun edi20RestClient(b: Builder, cfg: EDI20Config, @Qualifier(EDI20) interceptor: ClientHttpRequestInterceptor) =
        restClient(b, cfg, interceptor)

    @Bean
    @Qualifier(EDI20DEFT)
    fun ediDeft20RestClient(b: Builder, cfg: EDI20DeftConfig,
                            @Qualifier(EDI20) interceptor: ClientHttpRequestInterceptor) =
        restClient(b, cfg, interceptor)

    @Bean
    fun keyPair() =
        ECKeyGenerator(P_256)
            .algorithm(Algorithm("EC"))
            .keyUse(SIGNATURE)
            .keyID("${UUID.randomUUID()}")
            .generate()

    private fun restClient(b: Builder, cfg: AbstractRestConfig, interceptor: ClientHttpRequestInterceptor) =
        b.baseUrl("${cfg.baseUri}")
            .requestInterceptors {
                it.add(interceptor)
            }.build()
}

@Component
@Qualifier(EDI20)
class DPoPEnabledTokenExchangingRequestInterceptor(private val generator: DPoPProofGenerator,
                                                   clientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager) :
    TokenExchangingRequestInterceptor(clientManager, DPOP) {

    override fun intercept(req: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution) =
        with(req) {
            authorize(this)?.let {
                headers.set(DPOP.value, generator.proofFor(method, uri, it.accessToken))
            }
            execution.execute(this, body)
        }
}




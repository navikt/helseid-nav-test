package no.nav.helseidnavtest.security

import com.nimbusds.jose.jwk.JWK
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.DELEGATING
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_1
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.EDI_2
import no.nav.helseidnavtest.edi20.EDI20Config.Companion.PLAIN
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.util.LinkedMultiValueMap
import java.time.Instant.now

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(
    @Value("\${helse-id.test1.jwk}") private val jwk1: String,
    @Value("\${helse-id.test2.jwk}") private val jwk2: String
) {

    private val edi20_1_jwk = JWK.parse(jwk1)
    private val edi20_2_jwk = JWK.parse(jwk2)

    private val log = getLogger(SecurityConfig::class.java)

    @Bean
    @ConditionalOnProperty(name = ["spring.security.oauth2.resourceserver.jwt.jwk-set-uri"])
    @Primary
    fun jwtDecoder(oAuth2ResourceServerProperties: OAuth2ResourceServerProperties): NimbusJwtDecoder {
        log.info("URI " + oAuth2ResourceServerProperties.jwt.jwkSetUri)
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(oAuth2ResourceServerProperties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(JwtTypeValidator("at+jwt"))
        return jwtDecoder
    }

    private class JwtTypeValidator(private val expectedType: String) : OAuth2TokenValidator<Jwt> {

        override fun validate(token: Jwt): OAuth2TokenValidatorResult {
            val actualType = token.headers["typ"] as? String
            return if (actualType == expectedType) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error(
                        "invalid_token",
                        "Expected token type $expectedType but got $actualType",
                        null
                    )
                )
            }
        }
    }

    /*
    @Bean
    fun jwtDecoderByJwkKeySetUri(oAuth2ResourceServerProperties: OAuth2ResourceServerProperties,
                                 oAuth2ClientDetailProperties: OAuth2ClientResourceDetailProperties): JwtDecoder {
        val jwtProperties = oAuth2ResourceServerProperties.jwt

        val nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtProperties.jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.from(jwtProperties.jwsAlgorithms[0]))
            .jwtProcessorCustomizer { processor: ConfigurableJWTProcessor<SecurityContext?> ->
                processor.setJWSTypeVerifier(
                    DefaultJOSEObjectTypeVerifier(
                        JOSEObjectType("JWT"), JOSEObjectType("at+jwt")))
            }
            .build()

        val validators: MutableList<OAuth2TokenValidator<Jwt>> = ArrayList<OAuth2TokenValidator<Jwt>>()

        validators.add(JwtTimestampValidator())

        if (jwtProperties.issuerUri != null) {
            validators.add(JwtIssuerValidator(jwtProperties.issuerUri))
        }

        if (oAuth2ClientDetailProperties.getAudience() != null) {
            validators.add(JwtAudienceValidator(oAuth2ClientDetailProperties.getAudience()))
        }

        if (oAuth2ClientDetailProperties.getScope() != null) {
            validators.add(JwtScopeValidator(oAuth2ClientDetailProperties.getScope()))
        }

        nimbusJwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator<Any?>(validators))
        return nimbusJwtDecoder
    }
*/
    @Bean
    fun userAuthoritiesMapper() = GrantedAuthoritiesMapper { authorities ->
        authorities + authorities.flatMapTo(mutableSetOf()) { authority ->
            if (authority is OidcUserAuthority) {
                with(ClaimsExtractor(authority.userInfo.claims + authority.idToken.claims)) {
                    professions.map { p ->
                        SimpleGrantedAuthority("${p}_${securityLevel}").also {
                            log.info("La til rolle: $it")
                        }
                    }
                }
            } else {
                emptyList()
            }
        }
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        repo: ClientRegistrationRepository
    ): SecurityFilterChain {
        http {
            csrf { disable() }
            oauth2ResourceServer {
                jwt {
                }
            }
            oauth2Client {
            }
            authorizeRequests {
                authorize("/hello1", authenticated)
                authorize("/hello", hasAuthority("LE_4"))
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }

    @Bean
    @Qualifier(PLAIN)
    fun plainClientCredentialsTokenResponseClient() = DefaultClientCredentialsTokenResponseClient()

    @Bean
    fun traceRepo() = InMemoryHttpExchangeRepository()

    @Bean
    fun clientCredentialsRequestEntityConverter() = OAuth2ClientCredentialsGrantRequestEntityConverter().apply {
        addParametersConverter(
            NimbusJwtClientAuthenticationParametersConverter<OAuth2ClientCredentialsGrantRequest>(jwkResolver()
            ).apply {
                setJwtClientAssertionCustomizer { it.claims.notBefore(now()) }
            })
        addParametersConverter {
            LinkedMultiValueMap<String, String>().apply {
                this[CLIENT_ID] = it.clientRegistration.clientId
            }
        }
    }

    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(@Qualifier(DELEGATING) responseClient: OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest>,
                                                             repo: ClientRegistrationRepository,
                                                             service: OAuth2AuthorizedClientService) =
        AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service).apply {
            setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .refreshToken()
                .clientCredentials {
                    it.accessTokenResponseClient(responseClient)
                }
                .build()
            )
        }

    private fun jwkResolver(): (ClientRegistration) -> JWK = {
        if (it.clientAuthenticationMethod == PRIVATE_KEY_JWT) {
            when (it.registrationId) {
                EDI_1.second -> edi20_1_jwk
                EDI_2.second -> edi20_2_jwk
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        } else {
            throw IllegalArgumentException("Ukjent autentiseringsmetode: ${it.clientAuthenticationMethod}")
        }
    }
}

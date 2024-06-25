package no.nav.helseidnavtest.security

import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.log
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpHeaders
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
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.util.LinkedMultiValueMap
import java.security.PrivateKey
import java.time.Instant.now
import java.util.*


@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String,@Value("\${helse-id.test1.jwk}") private val test1: String) {

    private val authorizationEndpoint: String = "/oauth2/authorization"

    private val jwk = JWK.parse(assertion)
    private val jwk1 = JWK.parse(test1)


    private val log = getLogger(SecurityConfig::class.java)

    @Bean
    fun userAuthoritiesMapper() = GrantedAuthoritiesMapper { authorities ->
        authorities + authorities.flatMapTo(mutableSetOf()) { authority ->
            if (authority is OidcUserAuthority) {
                with(ClaimsExtractor(authority.userInfo.claims)) {
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
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply {
            setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/helse-id")
        }

    private fun requestEntityConverter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        addHeadersConverter(HeaderConverter())
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter {
            when (it.registrationId) {
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        })
    }

    private fun authCodeResponseClient(converter: OAuth2AuthorizationCodeGrantRequestEntityConverter) =
        DefaultAuthorizationCodeTokenResponseClient().apply {
           setRequestEntityConverter(converter)
        }


    @Bean
    fun securityFilterChain(http: HttpSecurity, repo: ClientRegistrationRepository, successHandler: LogoutSuccessHandler): SecurityFilterChain {
        http {
            oauth2Login {
                authorizationEndpoint {
                    baseUri = authorizationEndpoint
                    authorizationRequestResolver = pkceAddingResolver(repo)
                }
                tokenEndpoint {
                    accessTokenResponseClient = authCodeResponseClient(requestEntityConverter())
                }
            }
            oauth2ResourceServer {
                jwt {}
            }
            oauth2Client {}
            logout {
                logoutSuccessHandler = successHandler
            }
            authorizeRequests {
                authorize("/hello1", authenticated)
                authorize("/public/**", permitAll)
                authorize("/pdl", permitAll)
                authorize("/xml/**", permitAll)
                authorize("/monitoring/**", permitAll)
                authorize("/hello", hasAuthority("LE_4"))
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }


    private fun pkceAddingResolver(repo: ClientRegistrationRepository) =
        DefaultOAuth2AuthorizationRequestResolver(repo, authorizationEndpoint).apply {
            setAuthorizationRequestCustomizer(withPkce())
        }
    @Bean
    fun traceRepo() = InMemoryHttpExchangeRepository()

    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(repo: ClientRegistrationRepository, service: OAuth2AuthorizedClientService) =
        AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service).apply {
            setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                    .clientCredentials { p ->
                        val requestEntityConverter = OAuth2ClientCredentialsGrantRequestEntityConverter().apply {
                            addParametersConverter(NimbusJwtClientAuthenticationParametersConverter<OAuth2ClientCredentialsGrantRequest>(jwkResolver()).apply {
                                setJwtClientAssertionCustomizer { it.claims.notBefore(now()) }
                            })
                            addParametersConverter {
                                LinkedMultiValueMap<String,String>().apply {
                                    this[CLIENT_ID] = it.clientRegistration.clientId
                                }
                            }
                        }
                        p.accessTokenResponseClient(DefaultClientCredentialsTokenResponseClient().apply {
                            setRequestEntityConverter(requestEntityConverter)
                        })
                    }
                    .build()
            )
        }

    private fun jwkResolver(): (ClientRegistration) -> JWK = {
        if (it.clientAuthenticationMethod == PRIVATE_KEY_JWT) {
            when (it.registrationId) {
                "edi20-1" -> jwk1.also { log.info("Klient: edi20-1") }
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        } else {
            throw IllegalArgumentException("Ukjent autentiseringsmetode: ${it.clientAuthenticationMethod}")
        }
    }
}
class DPoPTokenGenerator(private val privateKey: PrivateKey) {

    fun generateDPoPToken(method: String, uri: String): String {
        val signer = RSASSASigner(privateKey)
        val claimsSet = JWTClaimsSet.Builder()
            .claim("htm", method)
            .claim("htu", uri)
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date())
            .build()

        val signedJWT = SignedJWT(JWSHeader.Builder(RS256).build(), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}

class HeaderConverter : Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> {
    override fun convert(source: OAuth2AuthorizationCodeGrantRequest): HttpHeaders {
       return HttpHeaders().also { log.info("HeaderConverter empty") }
    }

}
/*
class DPoPRequestEntityConverter(
    private val delegate: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>,
    private val dPoPTokenGenerator: DPoPTokenGenerator
) :
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    fun getAccessToken(userRequest: OAuth2AuthorizationCodeGrantRequest): OAuth2AccessToken {
        val accessToken: OAuth2AccessToken = delegate.getTokenResponse(userRequest).accessToken

        try {
            val dpopToken = dPoPTokenGenerator.generateDPoPToken(
                userRequest.getHttpMethod().name(),
                userRequest.getUri().toString()
            )

            val headers: HttpHeaders = HttpHeaders()
            headers.add("DPoP", dpopToken)

            val requestEntity: RequestEntity<*> = RequestEntity
                .get(URI(userRequest.getUri().toString()))
                .headers(headers)
                .build()

            // Send the request with DPoP headers
            // ...
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate DPoP token", e)
        }

        return accessToken
    }

    override fun getTokenResponse(authorizationGrantRequest: OAuth2AuthorizationCodeGrantRequest?): OAuth2AccessTokenResponse {
        TODO("Not yet implemented")
    }
}*/
package no.nav.helseidnavtest.security

import com.nimbusds.jose.jwk.JWK
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.ClientAuthenticationMethod.*
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.util.LinkedMultiValueMap


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

    //@Bean
    fun requestEntityConverter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter {
            when (it.registrationId) {
                "edi20-1" -> jwk1
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        })
    }

    //@Bean
    fun authCodeResponseClient(converter: OAuth2AuthorizationCodeGrantRequestEntityConverter) =
        DefaultAuthorizationCodeTokenResponseClient().apply {
            setRequestEntityConverter(converter)
        }

    //@Bean
    fun pkceResolver(repo: ClientRegistrationRepository) =
        DefaultOAuth2AuthorizationRequestResolver(repo, authorizationEndpoint).apply {
            setAuthorizationRequestCustomizer(withPkce())
        }

    @Bean
    fun securityFilterChain(http: HttpSecurity, /*resolver: OAuth2AuthorizationRequestResolver,*/ successHandler: LogoutSuccessHandler
    ): SecurityFilterChain {
        http {
            oauth2Login {
                authorizationEndpoint {
                    baseUri = authorizationEndpoint
                   // authorizationRequestResolver = resolver
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

    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(repo: ClientRegistrationRepository, service: OAuth2AuthorizedClientService) = AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service).apply {
        setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials {
                    val jwkResolver: (ClientRegistration) -> JWK? = { clientRegistration ->
                        if (clientRegistration.clientAuthenticationMethod == PRIVATE_KEY_JWT) {
                            when (clientRegistration.registrationId) {
                                "edi20-1" -> jwk1.also {  log.info("Klient: edi20") }
                                "helse-id" -> jwk.also {  log.info("Klient: helseid") }
                                else -> throw IllegalArgumentException("Ukjent klient: ${clientRegistration.registrationId}")
                            }
                        } else {
                            null
                        }
                    }

                    val requestEntityConverter = OAuth2ClientCredentialsGrantRequestEntityConverter()
                    val parametersConverter = NimbusJwtClientAuthenticationParametersConverter<OAuth2ClientCredentialsGrantRequest>(jwkResolver)
                    requestEntityConverter.addParametersConverter(parametersConverter)
                    requestEntityConverter.addParametersConverter { source ->
                        val parameters = LinkedMultiValueMap<String, String>()
                        parameters[CLIENT_ID] = source.clientRegistration.clientId
                        parameters
                    }
                    val responseClient = DefaultClientCredentialsTokenResponseClient()
                    responseClient.setRequestEntityConverter(requestEntityConverter)
                    it.accessTokenResponseClient(responseClient)
                }
                .build()
        )
    }
}
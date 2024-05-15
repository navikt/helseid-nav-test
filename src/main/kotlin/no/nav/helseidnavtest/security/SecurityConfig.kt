package no.nav.helseidnavtest.security

import com.nimbusds.jose.jwk.JWK

import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler


@Configuration
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String) {


    private val authorizationEndpoint: String = "/oauth2/authorization"

    private val jwk = JWK.parse(assertion)

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

    @Bean
    fun requestEntityConverter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter { reg ->
            when (reg.registrationId) {
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Unknown client: ${reg.registrationId}")
            }
        })
    }

    @Bean
    fun authCodeResponseClient(converter: OAuth2AuthorizationCodeGrantRequestEntityConverter) =
        DefaultAuthorizationCodeTokenResponseClient().apply {
            setRequestEntityConverter(converter)
        }

    @Bean
    fun pkceResolver(repo: ClientRegistrationRepository) =
        DefaultOAuth2AuthorizationRequestResolver(repo, authorizationEndpoint).apply {
            setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce())
        }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        resolver: OAuth2AuthorizationRequestResolver,
        successHandler: LogoutSuccessHandler
    ): SecurityFilterChain {
        http {
            oauth2Login {
                authorizationEndpoint {
                    baseUri = authorizationEndpoint
                    authorizationRequestResolver = resolver
                }
            }
            oauth2ResourceServer {
                jwt {
                }
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
                authorize("/actuator/**", permitAll)
                authorize("/hello", hasAuthority("LE_4"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }
}
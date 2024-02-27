package no.nav.helse.helseidnavtest.security

import com.nimbusds.jose.jwk.JWK
import no.nav.helse.helseidnavtest.helseopplysninger.HPRDetailsExtractor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler


@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String) {

    private val authorizationEndpoint: String = "/oauth2/authorization"

    private val jwk = JWK.parse(assertion)

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    private fun oidcUserService() = OAuth2UserService<OidcUserRequest, OidcUser> { req ->
        with(OidcUserService().loadUser(req)) {
            val level = claims["helseid://claims/identity/security_level"]
            val roles = HPRDetailsExtractor().extract(claims["helseid://claims/hpr/hpr_details"] as Map<*, *>).map {
                SimpleGrantedAuthority("${it.profession}_${level}")
            }.also {
                log.info("La til roller: $it")
            }
            DefaultOidcUser(authorities + roles, req.idToken, userInfo)
        }
    }

    @Bean
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply {
            setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/helse-id")
        }

    @Bean
    fun requestEntityConverter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter { clientRegistration ->
            when (clientRegistration.registrationId) {
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Unknown client: ${clientRegistration.registrationId}")
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
                userInfoEndpoint {
                    oidcUserService = oidcUserService()
                }
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
                authorize("/actuator/**", permitAll)
                authorize("/hello", hasAuthority("LE_4"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }

}
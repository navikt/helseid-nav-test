package no.nav.helse.helseidnavtest.security

import com.nimbusds.jose.jwk.JWK
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.*
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor
import no.nav.helse.helseidnavtest.helseopplysninger.ClaimsExtractor.Companion.oidcUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.context.SecurityContextHolder.*
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
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
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
            val extractor = ClaimsExtractor(this.claims)
            val roles = extractor.professions.map {
                SimpleGrantedAuthority("${it}_${extractor.securityLevel}").also {
                  //  log.info("La til roller: $it")
                }
            }
            DefaultOidcUser(authorities /* + roles,*/, req.idToken, userInfo)
        }
    }


    @Bean
    fun userAuthoritiesMapper(): GrantedAuthoritiesMapper = GrantedAuthoritiesMapper { authorities: Collection<GrantedAuthority> ->
        val mappedAuthorities = mutableSetOf<GrantedAuthority>()

        authorities.forEach { authority ->
            if (authority is OidcUserAuthority) {
                log.trace("MAP OidcUserAuthority: {}", authority)
                val extractor = ClaimsExtractor(authority.userInfo.claims)
                mappedAuthorities.addAll(extractor.professions.map { it ->
                    SimpleGrantedAuthority("${it}_${extractor.securityLevel}").also {
                        log.info("La til roller: $it")
                    }
                })
                val userInfo = authority.userInfo
                // Map the claims found in idToken and/or userInfo
                // to one or more GrantedAuthority's and add it to mappedAuthorities
            } else if (authority is OAuth2UserAuthority) {
                log.trace("MAP OAuth2UserAuthority: {}", authority)
                val userAttributes = authority.attributes
                // Map the attributes found in userAttributes
                // to one or more GrantedAuthority's and add it to mappedAuthorities
            }
        }

        (mappedAuthorities + authorities).also { log.info("Mapped authorities: $it") }
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
            exceptionHandling {
                accessDeniedHandler = CustomAccessDeniedHandler()
            }
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
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(req : HttpServletRequest, res : HttpServletResponse, e : AccessDeniedException) {
        getContext().authentication?.let {
            val professions = ClaimsExtractor((it.oidcUser().claims)).professions
            res.status = SC_FORBIDDEN;
            res.contentType = APPLICATION_JSON_VALUE;
            res.writer.write("Error : To access this resource you need to be a GP registered in HPR, but only the following were found: $professions")
        }
    }
}
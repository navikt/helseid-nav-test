package no.nav.helse.helseidnavtest

import com.nimbusds.jose.jwk.JWK
import kotlin.math.log
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import no.nav.boot.conditionals.Cluster.Companion.profiler

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class HelseIdNavTestApplication

fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String) {
    private val jwk = JWK.parse(assertion)

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    @Bean
     fun userAuthoritiesMapper() =
         GrantedAuthoritiesMapper { authorities : Collection<GrantedAuthority> ->
            val mappedAuthorities  = mutableSetOf<GrantedAuthority>()
            authorities.forEach { authority ->
                log.warn("Authority {}", authority.authority)
                mappedAuthorities.add(authority)
                if (OidcUserAuthority::class.java.isInstance(authority)) {
                    val oidcUserAuthority = authority as OidcUserAuthority
                    val idToken = oidcUserAuthority.idToken
                    if (authority.authority == "OIDC_USER") {
                        val level = idToken.getClaim<String>("helseid://claims/identity/security_level")
                        val hpr = idToken.getClaim<String>("helseid://claims/hpr/hpr_number")
                        if ("4" == level && hpr != null) {
                            mappedAuthorities.add(GrantedAuthority { "LEGE_4" })
                        }
                    }
                }
            }
            mappedAuthorities
        }
    @Bean
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply {
            setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/helseid" )
        }

    @Bean
    fun requestEntityConverter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter {
            when (it.registrationId) {
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Unknown client: ${it.registrationId}")
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
        DefaultOAuth2AuthorizationRequestResolver(repo, "/protected").apply {
            setAuthorizationRequestCustomizer(withPkce())
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
                    baseUri = "/protected"
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
                authorize("/hello", hasAuthority("LEGE_4"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }


}

@RestController
@EnableMethodSecurity(prePostEnabled = true)
class HelseController {

    private val log = LoggerFactory.getLogger(HelseController::class.java)

    @GetMapping("/")
    fun rickroll()  =  ModelAndView("redirect:https://www.youtube.com/watch?v=dQw4w9WgXcQ")

    @GetMapping("/error")
    fun error()   = rickroll()

    @GetMapping("/hello1")
    fun hello1(authentication: Authentication): String {
        val oidcUser = authentication.principal as OidcUser
        val attributes = oidcUser.attributes
        val idToken = oidcUser.idToken
        log.info(oidcUser.userInfo.claims.toString())
        log.warn("Logget inn bruker (pseudo) personnummer: {}", attributes["helseid://claims/identity/pid_pseudonym"])
        log.warn("{}", idToken.tokenValue)
        idToken.claims.forEach { log.warn("{}", it) }
        val auths = oidcUser.authorities.joinToString { it.authority }

        return """
            <p>Hello from <b>${attributes["name"]}</b></p>
            <p>HPR-nummer: <b>${attributes["helseid://claims/hpr/hpr_number"]}</b></p>
            <p>Nivå: <b>${attributes["helseid://claims/identity/assurance_level"]}</b> - <b>${attributes["helseid://claims/identity/security_level"]}</b></p>
            <p>Verifisert med: <b>${attributes["idp"]}</b></p>
            <p>Authorities <b>$auths}</b></p>

            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }
    @GetMapping("/public/utlogget")
    fun public() = "Du er nå logget ut. <a href='/hello'>Logg inn igjen</a>"

    @GetMapping("/hello")
    fun hello(authentication: Authentication): String {
        val oidcUser = authentication.principal as OidcUser
        val attributes = oidcUser.claims
        val idToken = oidcUser.idToken
        val auths = oidcUser.authorities.joinToString { it.authority }
        println("AAA")
        log.warn("Logget inn bruker (pseudo) personnummer: {}", attributes["helseid://claims/identity/pid_pseudonym"])
        log.warn("{}", idToken.tokenValue)
        idToken.claims.forEach { log.warn("{}", it) }

        return """
            <p>Hello from <b>${attributes["name"]}</b></p>
            <p>HPR-nummer: <b>${attributes["helseid://claims/hpr/hpr_number"]}</b></p>
            <p>Nivå: <b>${attributes["helseid://claims/identity/assurance_level"]}</b> - <b>${attributes["helseid://claims/identity/security_level"]}</b></p>
            <p>Verifisert med: <b>${attributes["idp"]}</b></p>
            <p>Authorities <b>$auths}</b></p>
            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }
}
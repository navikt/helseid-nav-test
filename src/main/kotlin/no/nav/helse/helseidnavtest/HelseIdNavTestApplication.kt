package no.nav.helse.helseidnavtest

import com.nimbusds.jose.jwk.JWK
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

    private val authorizationEndpoint: String = "/oauth2/authorization"


    private val jwk = JWK.parse(assertion)

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
     fun userAuthoritiesMapper() =
         GrantedAuthoritiesMapper { authorities ->
            val mappedAuthorities  = mutableSetOf<GrantedAuthority>()
            authorities.forEach {
                mappedAuthorities.add(it)
                when(it) {
                    is OidcUserAuthority -> {
                        val idToken = it.idToken
                        if (it.authority == "OIDC_USER") {
                            val level = idToken.getClaim<String>("helseid://claims/identity/security_level")
                            val hpr = idToken.getClaim<String>("helseid://claims/hpr/hpr_number")
                            if ("4" == level && hpr != null) {
                                mappedAuthorities.add(GrantedAuthority { "LEGE_4" })
                            }
                        }
                    }
                    else -> log.warn("Authority: {}", it)
                }
            }
            mappedAuthorities
        }
    @Bean
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply {
            setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/helse-id" )
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
                authorize("/hello", hasAuthority("LEGE_4"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }


}

@RestController
class HelseController {

    private val log = LoggerFactory.getLogger(HelseController::class.java)

    @GetMapping("/")
    fun root()  =  roll()

    @GetMapping("/error")
    fun error()   = roll()

    private fun roll() = ModelAndView("redirect:https://www.youtube.com/watch?v=dQw4w9WgXcQ")

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
        oidcUser.userInfo.claims.forEach { log.warn("{}", it) }
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
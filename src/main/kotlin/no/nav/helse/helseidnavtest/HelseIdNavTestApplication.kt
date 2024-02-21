package no.nav.helse.helseidnavtest

import com.nimbusds.jose.jwk.JWK
import no.nav.boot.conditionals.Cluster.Companion.profiler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableWebSecurity
class HelseIdNavTestApplication

fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}

@Configuration
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String) {
    private val jwk = JWK.parse(assertion)

    @Bean
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply { setPostLogoutRedirectUri("http://www.vg.no") }

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
            oauth2Client {}
            logout {
                logoutSuccessHandler = successHandler
            }
            authorizeRequests {
                authorize("/public/**", permitAll)
                authorize("/actuator/**", permitAll)
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
    fun root() = "root"

    @GetMapping("/public/utlogget")
    fun public() = "Du er nå logget ut. <a href='/hello'>Logg inn igjen</a>"

    @GetMapping("/hello")
    fun hello(authentication: Authentication): String {
        val oidcUser = authentication.principal as OidcUser
        val attributes = oidcUser.attributes
        val idToken = oidcUser.idToken

        log.warn("Logget inn bruker (pseudo) personnummer: {}", attributes["helseid://claims/identity/pid_pseudonym"])
        log.warn("{}", idToken.tokenValue)

        return """
            <p>Hello from <b>${attributes["name"]}</b></p>
            <p>HPR-nummer: <b>${attributes["helseid://claims/hpr/hpr_number"]}</b></p>
            <p>Nivå: <b>${attributes["helseid://claims/identity/assurance_level"]}</b> - <b>${attributes["helseid://claims/identity/security_level"]}</b></p>
            <p>Verifisert med: <b>${attributes["idp"]}</b></p>
                
            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }
}
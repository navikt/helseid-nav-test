package no.nav.helse.helseidnavtest

import com.nimbusds.jose.jwk.JWK
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.helse.helseidnavtest.SecurityConfig.HPRDetailsExtractor.HPRDetails.*
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
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class HelseIdNavTestApplication

fun main(args: Array<String>) {
    runApplication<HelseIdNavTestApplication>(*args) {
        setAdditionalProfiles(*profiler())
    }
}


//{approvals=[
// {profession=LE,
//     authorization={value=1, description=Autorisasjon},
//     requisition_rights=[
//        {value=1, description=Full rekvisisjonsrett}
//     ],
//     specialities=[
//         {value=40, description=Psykiatri}
//  ]
//  },
//  {profession=FT,
//     authorization={value=1, description=Autorisasjon},
//     requisition_rights=[],
//     specialities=[]
//  },
//  {profession=SP,
//     authorization={value=1, description=Autorisasjon},
//     requisition_rights=[], specialities=[]
//  }
// ],
// hpr_number=6081940}


//HPRDetails(profession=LE,
//   auth=HPRAuthorization(data=HPRData(value=1, description=Autorisasjon)),
//   rek=HPRRekvisision(data=[HPRData(value=value, description=1), HPRData(value=description, description=Full rekvisisjonsrett)]),
//   spec=HPRSpesialitet(data=[HPRData(value=value, description=40), HPRData(value=description, description=Psykiatri)]))

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String) {

    internal class HPRDetailsExtractor {

        fun extract(respons: Any?): List<HPRDetails> {
            val details = (respons as Map<*, *>)
            println(details)
            return (details["approvals"] as List<*>).map { app ->
                app as Map<*, *>
                val prof = app["profession"] as String
                val auth = app["authorization"] as Map<String, String>
                val req = app["requisition_rights"] as List<Map<String, String>>
                val spec = app["specialities"] as List<Map<String, String>>
                val authData = HPRAuthorization(HPRData(auth["value"].toString(), auth["description"].toString()))
                val rekvData = req.map { ex(it) }.flatten()
                val specData = spec.map { ex(it) }.flatten()
                HPRDetails(prof, authData, HPRRekvisision(rekvData), HPRSpesialitet(specData)).also {
                    println(it)
                }
            }
        }

        private fun ex(m: Map<String, String>) = m.map { (k, v) -> HPRData(k, v) }
        data class HPRDetails(
            val profession: String,
            val auth: HPRAuthorization,
            val rek: HPRRekvisision,
            val spec: HPRSpesialitet
        ) {
            data class HPRAuthorization(val data: HPRData)
            data class HPRRekvisision(val data: List<HPRData>)
            data class HPRSpesialitet(val data: List<HPRData>)
            data class HPRData(val value: String, val description: String)
        }
    }

    private val authorizationEndpoint: String = "/oauth2/authorization"


    private val jwk = JWK.parse(assertion)

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    private fun oidcUserService() = OAuth2UserService<OidcUserRequest, OidcUser> { req ->
        with(OidcUserService().loadUser(req)) {
            val details = HPRDetailsExtractor().extract(claims["helseid://claims/hpr/hpr_details"])
            val level = claims["helseid://claims/identity/security_level"]
            val profession = claims["helseid://claims/hpr/hpr_details"]?.let { it as Map<*, *> }?.let {
                ((it["approvals"] as List<*>?)?.getOrNull(0) as? Map<*, *>?)?.get("profession")
            }
            log.info("Level: $level, profession: $profession")
            val extra = if (level != null && profession != null) {
                mutableListOf(SimpleGrantedAuthority("${profession}_$level")).also {
                    log.info("La til rolle $it")
                }
            } else {
                mutableListOf()
            }
            DefaultOidcUser(
                authorities + extra /*+ SimpleGrantedAuthority("LEGE_4")*/, req.idToken, userInfo
            ).also {
                log.info("User: $this Authorities: $authorities")
                log.info("User: $it Authorities: ${it.authorities}")
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
                authorize("/hello", hasAuthority("SP_4 or LE_4"))
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }

}

@RestController
class HelseController {

    private fun roll() = ModelAndView("redirect:https://www.youtube.com/watch?v=dQw4w9WgXcQ")

    @GetMapping("/")
    fun root() = roll()

    @GetMapping("/error")
    fun error() = roll()

    @GetMapping("/hello1")
    fun hello1(authentication: Authentication): String {
        val oidcUser = authentication.principal as OidcUser

        val attributes = oidcUser.claims
        val scopes = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }

        val claims = oidcUser.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello1</h1>
            <p>Hello from <b>${attributes["name"]}</b></p>
            <p>HPR-nummer: <b>${attributes["helseid://claims/hpr/hpr_number"]}</b></p>
            <p>Nivå: <b>${attributes["helseid://claims/identity/assurance_level"]}</b> - <b>${attributes["helseid://claims/identity/security_level"]}</b></p>
            <p>Verifisert med: <b>${attributes["idp"]}</b></p>
            <br>
            <p>Requested authorities</p>
            <ul>$scopes</ul>
            <br>
            <p>Token claims</p>
            <ul>$claims</ul>
            <br>
            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }

    @GetMapping("/public/utlogget")
    fun public() = "Du er nå logget ut. <a href='/hello'>Logg inn igjen</a>"

    @GetMapping("/hello")
    fun hello(authentication: Authentication): String {
        val oidcUser = authentication.principal as OidcUser

        val attributes = oidcUser.claims
        val scopes = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }

        val claims = oidcUser.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello</h1>
            <p>Hello from <b>${attributes["name"]}</b></p>
            <p>HPR-nummer: <b>${attributes["helseid://claims/hpr/hpr_number"]}</b></p>
            <p>Nivå: <b>${attributes["helseid://claims/identity/assurance_level"]}</b> - <b>${attributes["helseid://claims/identity/security_level"]}</b></p>
            <p>Verifisert med: <b>${attributes["idp"]}</b></p>
            <br>
            <p>Requested authorities</p>
            <ul>$scopes</ul>
            <br>
            <p>Token claims</p>
            <ul>$claims</ul>
            <br>
            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }
}
package no.nav.helse.helseidnavtest.helseopplysninger

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class HelseopplysningerController {

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

        oidcUser.idToken.tokenValue
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
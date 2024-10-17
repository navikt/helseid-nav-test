package no.nav.helseidnavtest

import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.oidcUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Controller
class MainController {

    val searchResults = listOf("one", "two", "three", "four", "five")

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("results", searchResults)
        return "index"
    }

    @PostMapping("/clicked")
    fun clicked(model: Model): String {
        val user = SecurityContextHolder.getContext().authentication
        model.addAttribute("claims", TreeMap(user.oidcUser().claims))
        return "claims :: result"
    }
}

@RestController
class HelseopplysningerController {

    @GetMapping("/hello1")
    fun hello1(oidcUser: OidcUser) =
        dump(oidcUser)

    @GetMapping("/hello")
    fun hello(authentication: Authentication) =
        dump(authentication.oidcUser())

    private fun dump(oidcUser: OidcUser, scopes: Set<String> = emptySet()): String {
        val accessTokenScopes = scopes.joinToString("") {
            "<li>$it</li>"
        }
        val user = ClaimsExtractor(oidcUser).helsePersonell
        val authorities = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }
        val idtokenClaims = oidcUser.idToken.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")
        val userClaims = oidcUser.userInfo.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello1</h1>
            <p>Hello from <b>${user.navn}</b></p>
            <p>HPR-nummer: <b>${user.hprNumber}</b></p>
            <p>Nivå: <b>${user.assuranceLevel}</b> - <b>${user.securityLevel}</b></p>
            <br>
            <p>Access token scopes</p>
            <ul>$accessTokenScopes</ul>
            <p>Requested authorities</p>
            <ul>$authorities</ul>
            <br>
            <p>Userinfo claims</p>
            <ul>$userClaims</ul>
             <br>
         <p>ID-Token claims</p>
            <ul>$idtokenClaims</ul>     
            <br>
            <a href="/logout"><button>Logg ut</button></a>
        """.trimIndent()
    }
}
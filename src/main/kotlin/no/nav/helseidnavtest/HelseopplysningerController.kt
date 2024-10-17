package no.nav.helseidnavtest

import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.oidcUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
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
class HelseopplysningerController(private val authorizedClientService: OAuth2AuthorizedClientService) {

    @GetMapping("/hello1")
    fun hello1(authentication: Authentication) =
        dump(authentication as OAuth2AuthenticationToken)

    @GetMapping("/hello")
    fun hello(authentication: Authentication) =
        dump(authentication as OAuth2AuthenticationToken)

    private fun dump(token: OAuth2AuthenticationToken): String {
        val oidcUser = token.oidcUser()

        val client =
            authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(token.authorizedClientRegistrationId,
                token.name)

        val accessTokenScopes = client.accessToken?.scopes?.joinToString("") {
            "<li>$it</li>"
        }
        val user = ClaimsExtractor(oidcUser.claims).helsePersonell
        val authorities = oidcUser.authorities.joinToString("") {
            "<li>${it.authority}</li>"
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
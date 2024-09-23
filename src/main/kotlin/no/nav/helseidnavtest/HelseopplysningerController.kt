package no.nav.helseidnavtest

import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.oidcUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*

@Controller
class MainController {
    val searchResults = listOf("one", "two", "three", "four", "five")

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("results", searchResults)
        return "index"
    }

    @GetMapping("/search")
    fun search(q: String, model: Model): String {
        val filtered = searchResults.filter { it.startsWith(q.lowercase(Locale.getDefault())) }
        model.addAttribute("results", filtered)
        return "search :: results"
    }

    @PostMapping("/clicked")
    fun clicked(model: Model): String {
        val user = SecurityContextHolder.getContext().authentication.oidcUser()
        model.addAttribute("tidspunkt", user.authenticatedAt)
        model.addAttribute("now", LocalDateTime.now().toString())
        return "clicked :: result"
    }
}

@RestController
class HelseopplysningerController {

    @GetMapping("/hello1")
    fun hello1(authentication: Authentication) = dump(authentication)

    @GetMapping("/hello")
    fun hello(authentication: Authentication) = dump(authentication)

    private fun dump(authentication: Authentication): String {
        val oidcUser = authentication.oidcUser()
        val user = ClaimsExtractor(oidcUser).user
        val scopes = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }
        val claims = oidcUser.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello1</h1>
            <p>${authentication.javaClass}
            <p>${authentication.oidcUser().javaClass}
            <p>Hello from <b>${user.navn}</b></p>
            <p>HPR-nummer: <b>${user.hprNumber}</b></p>
            <p>Nivå: <b>${user.assuranceLevel}</b> - <b>${user.securityLevel}</b></p>
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
package no.nav.helseidnavtest

import no.nav.helseidnavtest.security.ClaimsExtractor
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelseopplysningerController {

    @GetMapping("/hello1")
    fun hello1(oidcUser: OidcUser) = dump(oidcUser)

    @GetMapping("/hello")
    fun hello(oidcUser: OidcUser) = dump(oidcUser)

    private fun dump(oidcUser: OidcUser): String {
        val user = ClaimsExtractor(oidcUser).user
        val scopes = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }
        val claims = oidcUser.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello1</h1>
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
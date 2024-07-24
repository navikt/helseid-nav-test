package no.nav.helseidnavtest


import no.nav.helseidnavtest.security.ClaimsExtractor
import no.nav.helseidnavtest.security.ClaimsExtractor.Companion.oidcUser
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelseopplysningerController {


    @GetMapping("/hello1")
    fun hello1(authentication: Authentication) = dump(authentication)

    @GetMapping("/hello")
    fun hello(authentication: Authentication) = dump(authentication)

    private fun dump(authentication: Authentication): String {
        val oidcUser = authentication.oidcUser()
        val extractor = ClaimsExtractor(oidcUser.claims)
        val scopes = oidcUser.authorities.joinToString("") {
            "<li>${it.authority.replace("SCOPE_", "")}</li>"
        }
        val claims = oidcUser.claims.map {
            "<li>${it.key}: ${it.value}</li>"
        }.joinToString("")

        return """
            <h1>/hello1</h1>
            <p>Hello from <b>${extractor.claim("name")}</b></p>
            <p>HPR-nummer: <b>${extractor.hprNumber}</b></p>
            <p>Nivå: <b>${extractor.assuranceLevel}</b> - <b>${extractor.securityLevel}</b></p>
            <p>Verifisert med: <b>${extractor.claim("idp")}</b></p>
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
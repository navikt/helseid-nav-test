package no.nav.helse.helseidnavtest.helseopplysninger.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse

 fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String) {
    throw when (res.statusCode) {
        HttpStatus.NOT_FOUND -> OppslagNotFoundException("Fant ikke noe for $detail")
        else -> IntegrationException("Fikk response ${res.statusCode} fra ${req.uri}")
    }
}
class IntegrationException(msg: String) : RuntimeException(msg)
class OppslagNotFoundException(msg: String) : RuntimeException(msg)
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
class IntegrationException(msg: String, cause: Throwable? =null) : RecoverableException(msg, cause)
class OppslagNotFoundException(msg: String) : IrrecoverableException(msg)

abstract class IrrecoverableException(msg: String, cause: Throwable? =null) : RuntimeException(msg, cause)
abstract class RecoverableException(msg: String, cause: Throwable? =null) : RuntimeException(msg, cause)
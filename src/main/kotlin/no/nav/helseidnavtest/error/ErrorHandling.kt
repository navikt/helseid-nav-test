package no.nav.helseidnavtest.error

import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.client.ClientHttpResponse

 fun handleErrors(req: HttpRequest, res: ClientHttpResponse, detail: String) {
    throw when (res.statusCode) {
        NOT_FOUND -> NotFoundException("Fant ikke noe for $detail")
        else -> RecoverableException("Fikk response ${res.statusCode} fra ${req.uri}")
    }
}
abstract class IntegrationException(msg: String, cause: Throwable? =null) : RuntimeException(msg, cause)
class NotFoundException(msg: String, cause: Throwable? = null) : IrrecoverableException(msg, cause)
 open class IrrecoverableException(msg: String, cause: Throwable? = null) : IntegrationException(msg, cause)
 class RecoverableException(msg: String, cause: Throwable? = null) : IntegrationException(msg, cause)
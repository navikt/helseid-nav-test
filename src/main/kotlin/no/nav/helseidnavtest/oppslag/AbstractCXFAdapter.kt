package no.nav.helseidnavtest.oppslag

import no.nav.helseidnavtest.health.Pingable
import org.apache.cxf.configuration.security.AuthorizationPolicy
import org.apache.cxf.endpoint.Client
import org.apache.cxf.ext.logging.LoggingInInterceptor
import org.apache.cxf.ext.logging.LoggingOutInterceptor
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPConduit

abstract class AbstractCXFAdapter<T>(val cfg: WSConfig) :Pingable {

    protected inline fun <reified T> client(): T {

        val factory = JaxWsProxyFactoryBean().apply {
            address = cfg.url.toString()
        }

        val service = factory.create(T::class.java)
        val client: Client = ClientProxy.getClient(service)

        // Add logging interceptors for debugging
        client.inInterceptors.add(LoggingInInterceptor())
        client.outInterceptors.add(LoggingOutInterceptor())

        // Set up HTTP authentication
        val httpConduit = client.conduit as HTTPConduit
        val authorizationPolicy = AuthorizationPolicy().apply {
            userName = cfg.username
            password = cfg.password
        }
        httpConduit.authorization = authorizationPolicy
        return service
    }


}

package no.nav.helseidnavtest.oppslag

import no.nav.helseidnavtest.health.Pingable
import org.apache.cxf.configuration.security.AuthorizationPolicy
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPConduit
import java.net.URI

abstract class AbstractCXFAdapter(val cfg: BasicAuthConfig) : Pingable {

    protected inline fun <reified T> client(): T {

        val service = JaxWsProxyFactoryBean().apply {
            address = "${cfg.url}"
            features.add(LoggingFeature().apply { setPrettyLogging(true) })

        }.create(T::class.java)

        val client = ClientProxy.getClient(service)
        (client.conduit as HTTPConduit).authorization = AuthorizationPolicy().apply {
            userName = cfg.username
            password = cfg.password
        }
        return service
    }

    override fun pingEndpoint() = "${cfg.url}"

}
abstract class BasicAuthConfig(val url: URI, val username: String, val password:  String)
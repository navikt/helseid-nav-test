package no.nav.helseidnavtest.oppslag

import no.nav.helseidnavtest.health.Pingable
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HttpConduitFeature
import java.net.URI

abstract class AbstractCXFAdapter(val cfg: BasicAuthConfig) : Pingable {

    protected inline fun <reified T> client(): T {

        val service = JaxWsProxyFactoryBean().apply {
            address = "${cfg.url}"
            with(features) {
                add(LoggingFeature().apply {
                    setPrettyLogging(true)
                    setSensitiveElementNames(setOf("Sex"))
                })
                add(HttpConduitFeature().apply { username = cfg.username; password = cfg.password})
            }

        }.create(T::class.java)
        return service
    }

    override fun pingEndpoint() = "${cfg.url}"

}
abstract class BasicAuthConfig(val url: URI, val username: String, val password:  String)

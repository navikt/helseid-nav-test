package no.nav.helse.helseidnavtest.helseopplysninger

import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPConduit

 inline fun <reified T> createPort(cfg: FastlegeConfig) = createPort<T>(cfg.url) {
    proxy {}
    port {
        withBasicAuth(cfg.username, cfg.password)
    }
}
inline fun <reified T> createPort(endpoint: String, extraConfiguration: PortConfigurator<T>.() -> Unit = {}): T = PortConfigurator<T>().let {
    extraConfiguration(it)
    (
        JaxWsProxyFactoryBean().apply {
            address = endpoint
            serviceClass = T::class.java
            it.proxyConfigurator(this)
        }.create() as T
        ).apply {
            it.portConfigurator(this)
        }
}


class PortConfigurator<T> {
    var proxyConfigurator: JaxWsProxyFactoryBean.() -> Unit = {}
    var portConfigurator: T.() -> Unit = {}

    fun proxy(configurator: JaxWsProxyFactoryBean.() -> Unit) {
        proxyConfigurator = configurator
    }

    fun port(configurator: T.() -> Unit) {
        portConfigurator = configurator
    }

    fun T.withBasicAuth(username: String, password: String) = apply {
        (ClientProxy.getClient(this).conduit as HTTPConduit).apply {
            authorization.userName = username
            authorization.password = password
        }
    }
}

abstract class FastlegeConfig(val url: String, val username: String, val password: String)
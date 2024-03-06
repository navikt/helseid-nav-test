package no.nav.helse.helseidnavtest.helseopplysninger.fastlege

import java.util.Date
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory.*
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPConduit
import org.springframework.stereotype.Component
import no.nav.helse.helseidnavtest.ws.IFlrReadOperations

@Component
class FastlegeWSAdapter(cfg: FastlegeConfig) {

    private val client : IFlrReadOperations = createPort(cfg)



    fun fastlege(hpr: Int, fnr: String) = client.confirmGP(fnr, hpr, now())

    fun fastlegeForPasient(fnr: String) = client.getPatientGPDetails(fnr)


    private fun now() = newInstance().newXMLGregorianCalendar(GregorianCalendar().apply {
        time = Date()
    })

    private final inline fun <reified IFlrReadOperations> createPort(cfg: FastlegeConfig) = createPort<IFlrReadOperations>(cfg.url) {
        proxy {}
        port {
            withBasicAuth(cfg.username, cfg.password)
        }
    }
    private final inline fun <reified T> createPort(endpoint: String, extraConfiguration: PortConfigurator<T>.() -> Unit = {}): T = PortConfigurator<T>().let {
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
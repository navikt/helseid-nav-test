package no.nav.helseidnavtest.dialogmelding

import jakarta.jms.ConnectionFactory
import jakarta.xml.bind.Marshaller.JAXB_ENCODING
import jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT
import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.health.AbstractPingableHealthIndicator
import no.nav.helseopplysninger.apprec.XMLAppRec
import no.nav.helseopplysninger.basecontainer.XMLBase64Container
import no.nav.helseopplysninger.dialogmelding.XMLDialogmelding
import no.nav.helseopplysninger.fellesformat2.XMLEIFellesformat
import no.nav.helseopplysninger.fellesformat2.XMLSporinformasjonBlokkType
import no.nav.helseopplysninger.hodemelding.XMLCV
import no.nav.helseopplysninger.hodemelding.XMLMsgHead
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.client.RestClient.Builder

@Configuration
class DialogmeldingClientBeanConfig {

    @Bean
    @Qualifier(DIALOGMELDING)
    fun restClientDialogmelding(builder: Builder, cfg: DialogmeldingConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @ConditionalOnProperty("$DIALOGMELDING.enabled", havingValue = "true", matchIfMissing = true)
    fun dialogmeldingdHealthIndicator(a: DialogmeldingRestAdapter) = object : AbstractPingableHealthIndicator(a) {}


    @Bean
    fun jaxb2Marshaller(): Jaxb2Marshaller = Jaxb2Marshaller().apply {
        setClassesToBeBound(XMLEIFellesformat::class.java,
            XMLSporinformasjonBlokkType::class.java,
            XMLMsgHead::class.java,
            XMLDialogmelding::class.java,
            XMLBase64Container::class.java,
            XMLAppRec::class.java)
        setJaxbContextProperties(mapOf(JAXB_FORMATTED_OUTPUT to true, JAXB_ENCODING to "UTF-8"))
    }

    @Bean
    fun xmlMessageConverter(marshaller: Jaxb2Marshaller) = MarshallingMessageConverter(marshaller, marshaller)


    @Bean
    fun jmsListenerContainerFactory(connectionFactory: ConnectionFactory, xmlMessageConverter: MessageConverter) =
        DefaultJmsListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(xmlMessageConverter)
        }

/*  TODO
    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory, xmlMessageConverter: MessageConverter) =
        JmsTemplate(connectionFactory).apply {
            messageConverter = xmlMessageConverter
        }
        */

}
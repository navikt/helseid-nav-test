package no.nav.helse.helseidnavtest
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory
import no.nav.helseidnavtest.dialogmelding.*
import no.nav.helseidnavtest.dialogmelding.DialogmeldingConfig.Companion.DIALOGMELDING
import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.person.Person.Navn
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.util.*


//@Testcontainers
@TestInstance(PER_CLASS)
@Import(DialogmeldingClientBeanConfig::class)
@TestPropertySource(locations = ["classpath:application-test.properties"], properties = ["dialogmelding.request=request","dialogmelding.base-uri=http://www.vg.no"])
@EnableConfigurationProperties(DialogmeldingConfig::class)
@ContextConfiguration(classes =  [ApprecSender::class,MQQueueConnectionFactory::class,DialogmeldingRestAdapter::class, ApprecMottaker::class])
@ExtendWith(SpringExtension::class)
@ExtendWith(MockitoExtension::class)
@AutoConfigureWebClient
@WebMvcTest
@ActiveProfiles("test")
class IBMMQTest {

    @Autowired
    lateinit var sender: JmsTemplate
    @Mock
    lateinit var adresse: AdresseRegisterClient
    @Autowired
    lateinit var cfg: DialogmeldingConfig


    @Value("\${ibm.mq.conn-name}")
    lateinit var qm: String

    @Autowired
    lateinit var a: DialogmeldingRestAdapter

    @AfterAll
    fun tearDown() {
        //x'ibmMqContainer.stop()
    }
    @Test
    fun testMQConnection() {
       sender.convertAndSend(cfg.request, msg())


        // Use IBM MQ client library to connect to the container
        // Example:
    // Perform your test logic here

        // Assert some conditions based on the test logic
        // Example: assertNotNull(queueManager);
    }


    fun msg()  {
        val kontor = BehandlerKontor("Et legekontor", "Fyrstikkalleen 1", Postnummer(1234),
            "Oslo", Orgnummer(123456789), PartnerId(42),HerId(12345678))

        val behandler =  Behandler(UUID.randomUUID(),
            Fødselsnummer("26900799232"), Navn("Ole", "Mellomnavn", "Olsen"),
            HerId(123456789), HprId(987654321), "12345678", kontor)

        val b = Dialogmelding(uuid = UUID.randomUUID(),
            behandler = behandler,
            id =  Fødselsnummer("26900799232"),
            conversationUuid =  UUID.randomUUID(),
            tekst = "dette er litt tekst",
            vedlegg = ClassPathResource("test.pdf").inputStream.readBytes(),
        )
        val arbeidstaker = Arbeidstaker(Fødselsnummer("03016536325"), Navn( "Ola", "Mellomnavn", "Olsen"))
        val m  = DialogmeldingMapper(adresse).xmlFra(b, arbeidstaker)
    }

    companion object {

       // @Container
       // @ServiceConnection
       // val ibmMqContainer = GenericContainer("ibmcom/mq:latest")
       //     .withExposedPorts(1414)
       //     .withStartupTimeout(Duration.ofMinutes(5)).apply {
        //        start()
        //    }

       // @DynamicPropertySource
       // @JvmStatic
        fun mq(registry: DynamicPropertyRegistry) {
          //  registry.add("ibm.mq.connName") { "${ibmMqContainer.host}(${ibmMqContainer.getMappedPort(1414)})" }
        }
    }
}
package no.nav.helse.helseidnavtest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.util.*


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IBMMQTest {
    @Test
    fun testMQConnection() {
        val host = ibmMqContainer.host
        val port = ibmMqContainer.getMappedPort(1414)


        // Use IBM MQ client library to connect to the container
        // Example:
        // MQQueueManager queueManager = new MQQueueManager("QM1", createConnectionProperties(host, port));
        // Perform your test logic here

        // Assert some conditions based on the test logic
        // Example: assertNotNull(queueManager);
    }

    // Helper method to create connection properties
    private fun createConnectionProperties(host: String, port: Int): Properties {
        val properties: Properties = Properties()
        properties.put("hostname", host)
        properties.put("port", port)
        properties.put("channel", "DEV.APP.SVRCONN")
        properties.put("queueManager", "QM1")
        properties.put("transportType", 1)
        return properties
    }

    companion object {
        @Container
        private val ibmMqContainer = GenericContainer("ibmcom/mq:latest")
            .withEnv("LICENSE", "accept")
            .withEnv("MQ_QMGR_NAME", "QM1")
            .withExposedPorts(1414, 9443)
            .withStartupTimeout(Duration.ofMinutes(5))

        @BeforeAll
       @JvmStatic
        fun setUp() {
            ibmMqContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            ibmMqContainer.stop()
        }
    }
}
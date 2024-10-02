package no.nav.helseidnavtest

import no.nav.helseidnavtest.oppslag.adresse.AdresseRegisterClient
import no.nav.helseidnavtest.oppslag.fastlege.FastlegeClient
import no.nav.helseidnavtest.oppslag.person.PDLClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.annotation.AnnotationRetention.RUNTIME

//@SpringBootTest
@ExtendWith(MockitoExtension::class)
@ExtendWith(SpringExtension::class)

class IBMMQTest {

    @Retention(RUNTIME)
    // @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
    annotation class WithMockCustomUser(val username: String = "rob", val name: String = "Rob Winch")

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    //@Autowired
    //lateinit var marshaller: Jaxb2Marshaller

    @Mock
    lateinit var adresse: AdresseRegisterClient

    @Mock
    lateinit var pdl: PDLClient

    @Mock
    lateinit var fastlege: FastlegeClient

    @Test
    fun testMQConnection() {
    }

    class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
        override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
            val context = SecurityContextHolder.createEmptyContext()
            //  val principal = OAuth2User(customUser.name, customUser.username)
            // val auth: Authentication =
            //       OAuth2AuthenticationToken(principal, listOf(SimpleGrantedAuthority("LE_4")), "1")
            //  context.authentication = auth
            return context
        }
    }
}

/*
@TestConfiguration
@SpringBootConfiguration
class TestConfig {
    @Bean
    fun jaxb2Marshaller() = Jaxb2Marshaller().apply {
        setClassesToBeBound(
            XMLEIFellesformat::class.java,
            XMLSporinformasjonBlokkType::class.java,
            XMLMsgHead::class.java,
            XMLDialogmelding::class.java,
            XMLBase64Container::class.java,
            XMLAppRec::class.java)
    }
}*/


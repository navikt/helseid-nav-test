package no.nav.helseidnavtest.security

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.Curve.*
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.helseidnavtest.oppslag.AbstractRestClientAdapter.Companion.log
import org.bouncycastle.cert.ocsp.Req
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.*
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import java.security.PrivateKey
import java.time.Instant
import java.time.Instant.now
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity(debug = true)
class SecurityConfig(@Value("\${helse-id.jwk}") private val assertion: String,@Value("\${helse-id.test1.jwk}") private val test1: String) {

    private val authorizationEndpoint: String = "/oauth2/authorization"

    private val jwk = JWK.parse(assertion)
    private val jwk1 = JWK.parse(test1)


    private val log = getLogger(SecurityConfig::class.java)

    @Bean
    fun userAuthoritiesMapper() = GrantedAuthoritiesMapper { authorities ->
        authorities + authorities.flatMapTo(mutableSetOf()) { authority ->
            if (authority is OidcUserAuthority) {
                with(ClaimsExtractor(authority.userInfo.claims)) {
                    professions.map { p ->
                        SimpleGrantedAuthority("${p}_${securityLevel}").also {
                            log.info("La til rolle: $it")
                        }
                    }
                }
            } else {
                emptyList()
            }
        }
    }
    @Bean
    fun oidcLogoutSuccessHandler(repo: ClientRegistrationRepository) =
        OidcClientInitiatedLogoutSuccessHandler(repo).apply {
            setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/helse-id")
        }

    private fun converter() = OAuth2AuthorizationCodeGrantRequestEntityConverter().apply {
        //addHeadersConverter(HeaderConverter())
        addParametersConverter(NimbusJwtClientAuthenticationParametersConverter {
            when (it.registrationId) {
                "helse-id" -> jwk
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        })
    }

    private fun authCodeResponseClient(converter: OAuth2AuthorizationCodeGrantRequestEntityConverter) =
        DefaultAuthorizationCodeTokenResponseClient().apply {
           setRequestEntityConverter(converter)
        }


    @Bean
    fun securityFilterChain(http: HttpSecurity, repo: ClientRegistrationRepository, successHandler: LogoutSuccessHandler): SecurityFilterChain {
        http {
            oauth2Login {
                authorizationEndpoint {
                    baseUri = authorizationEndpoint
                    authorizationRequestResolver = pkceAddingResolver(repo)
                }
                tokenEndpoint {
                    accessTokenResponseClient = DPoPAuthorizationCodeTokenRequestClient(DPoPUtils(),converter()) // authCodeResponseClient(converter())
                }
            }
            oauth2ResourceServer {
                jwt {}
            }
            oauth2Client {}
            logout {
                logoutSuccessHandler = successHandler
            }
            authorizeRequests {
                authorize("/hello1", authenticated)
                authorize("/public/**", permitAll)
                authorize("/pdl", permitAll)
                authorize("/xml/**", permitAll)
                authorize("/monitoring/**", permitAll)
                authorize("/hello", hasAuthority("LE_4"))
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }


    private fun pkceAddingResolver(repo: ClientRegistrationRepository) =
        DefaultOAuth2AuthorizationRequestResolver(repo, authorizationEndpoint).apply {
            setAuthorizationRequestCustomizer(withPkce())
        }
    @Bean
    fun traceRepo() = InMemoryHttpExchangeRepository()

    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(repo: ClientRegistrationRepository, service: OAuth2AuthorizedClientService) =
        AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service).apply {
            setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                    .clientCredentials { p ->
                        val requestEntityConverter = OAuth2ClientCredentialsGrantRequestEntityConverter().apply {
                            addParametersConverter(NimbusJwtClientAuthenticationParametersConverter<OAuth2ClientCredentialsGrantRequest>(jwkResolver()).apply {
                                setJwtClientAssertionCustomizer { it.claims.notBefore(now()) }
                            })
                            addParametersConverter {
                                LinkedMultiValueMap<String,String>().apply {
                                    this[CLIENT_ID] = it.clientRegistration.clientId
                                }
                            }
                        }
                        p.accessTokenResponseClient(DefaultClientCredentialsTokenResponseClient().apply {
                            setRequestEntityConverter(requestEntityConverter)
                        })
                    }
                    .build()
            )
        }

    private fun jwkResolver(): (ClientRegistration) -> JWK = {
        if (it.clientAuthenticationMethod == PRIVATE_KEY_JWT) {
            when (it.registrationId) {
                "edi20-1" -> jwk1.also { log.info("Klient: edi20-1") }
                else -> throw IllegalArgumentException("Ukjent klient: ${it.registrationId}")
            }
        } else {
            throw IllegalArgumentException("Ukjent autentiseringsmetode: ${it.clientAuthenticationMethod}")
        }
    }
}
class DPoPTokenGenerator(private val privateKey: PrivateKey) {

    fun generateDPoPToken(method: String, uri: String): String {
        val signer = RSASSASigner(privateKey)
        val claimsSet = JWTClaimsSet.Builder()
            .claim("htm", method)
            .claim("htu", uri)
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date())
            .build()

        val signedJWT = SignedJWT(JWSHeader.Builder(RS256).build(), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}

class HeaderConverter : Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> {
    override fun convert(source: OAuth2AuthorizationCodeGrantRequest): HttpHeaders {
       return HttpHeaders().also { log.info("HeaderConverter empty") }
    }

}


class DPoPAuthorizationCodeTokenRequestClient(
    private val dpopUtils: DPoPUtils,
    private val converter: OAuth2AuthorizationCodeGrantRequestEntityConverter,
) : OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
{
    private val mapResponseType: ParameterizedTypeReference<HashMap<String, String>> =
        object : ParameterizedTypeReference<HashMap<String, String>>()
        {}
    private val defaultAuthorizationCodeTokenResponseClient = DefaultAuthorizationCodeTokenResponseClient().apply {
        setRequestEntityConverter(converter)
    }
    override fun getTokenResponse(authorizationCodeGrantRequest: OAuth2AuthorizationCodeGrantRequest): OAuth2AccessTokenResponse
    {
        val oidcResponse = defaultAuthorizationCodeTokenResponseClient.getTokenResponse(authorizationCodeGrantRequest)
        val codeVerifier = authorizationCodeGrantRequest.authorizationExchange.authorizationRequest.getAttribute<String>("code_verifier")
        val code = authorizationCodeGrantRequest.authorizationExchange.authorizationResponse.code
        val tokenURI = authorizationCodeGrantRequest.clientRegistration.providerDetails.tokenUri
        val redirectURI = authorizationCodeGrantRequest.clientRegistration.redirectUri
        val clientId = authorizationCodeGrantRequest.clientRegistration.clientId


        //val sessionId = RequestContextHolder.currentRequestAttributes().sessionId
        val sessionKey = ECKeyGenerator(P_256)
            .algorithm(Algorithm("EC"))
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .generate()

        //dpopUtils.saveSessionKey(sessionId, sessionKey)



        val jwt = dpopUtils.dpopJWT("POST", tokenURI, sessionKey)

        val response = dpopResponse(codeVerifier, code, redirectURI, clientId, tokenURI, jwt)
        return OAuth2AccessTokenResponse.withToken(response["access_token"])
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .refreshToken(response["refresh_token"])
            .additionalParameters(oidcResponse.additionalParameters)
            .expiresIn(response["expires_in"]?.toLong() ?: 0)
            .build()
    }

    private fun dpopResponse(
        codeVerifier: String,
        code: String,
        redirectURI: String,
        clientId: String,
        tokenURI: String,
        jwt: String
    ): Map<String, String>
    {
        val headers = HttpHeaders()
        headers.add("DPoP", jwt)
        headers.add("Content-Type", "application/x-www-form-urlencoded")

        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "authorization_code")
        params.add("code_verifier", codeVerifier)
        params.add("code", code)
        params.add("redirect_uri", redirectURI)
        params.add("client_id", clientId)

        val httpEntity = HttpEntity<LinkedMultiValueMap<String, String>>(params, headers)

        return RestTemplate().exchange(
            tokenURI,
            HttpMethod.POST,
            httpEntity,
            mapResponseType
        ).body ?: emptyMap()
    }
}


class DPoPUtils
{
    private val sessionKeyMap: ConcurrentHashMap<String, ECKey> = ConcurrentHashMap()

    fun sessionKey(sessionId: String): ECKey? = sessionKeyMap[sessionId]

    fun saveSessionKey(sessionId: String, key: ECKey)
    {
        sessionKeyMap[sessionId] = key
    }

    fun removeSessionKey(sessionId: String)
    {
        sessionKeyMap.remove(sessionId)
    }

    fun dpopJWT(method: String, targetURI: String, sessionKey: ECKey): String =
        signedJWT(header(sessionKey), payload(method, targetURI), sessionKey)

    private fun signedJWT(header: JWSHeader, payload: JWTClaimsSet, sessionKey: ECKey): String
    {
        val signedJWT = SignedJWT(header, payload)
        signedJWT.sign(ECDSASigner(sessionKey.toECPrivateKey()))
        return signedJWT.serialize()
    }

    private fun payload(method: String, targetURI: String): JWTClaimsSet =
        JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date.from(Instant.now()))
            .claim("htm", method)
            .claim("htu", targetURI)
            .build()

    private fun header(sessionKey: ECKey): JWSHeader =
        JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType("dpop+jwt"))
            .jwk(sessionKey.toPublicJWK())
            .build();
}
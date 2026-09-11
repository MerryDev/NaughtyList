package net.neruxvace.naughtylist.backend.auth

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfiguration {

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun jwtSecretKey(properties: JwtProperties): SecretKey {
        val bytes = Base64.getDecoder().decode(properties.secret)

        require(bytes.size >= 32) { "JWT signing secret must be at least 32 bytes" }
        return SecretKeySpec(bytes, "HmacSHA256")
    }

    @Bean
    fun jwtEncoder(key: SecretKey): JwtEncoder {
        return NimbusJwtEncoder(ImmutableSecret(key))
    }

    @Bean
    fun jwtDecoder(key: SecretKey, properties: JwtProperties, secretKey: SecretKey): JwtDecoder {
        val decoder = NimbusJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

        val issuerValidator = JwtValidators.createDefaultWithIssuer(properties.issuer)
        val audienceValidator = JwtClaimValidator<List<String>>("audience") { audience -> properties.audience in audience }

        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(issuerValidator, audienceValidator))
        return decoder
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.requestMatchers("/api/v1/auth/token").permitAll().anyRequest().authenticated() }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }

        return http.build()
    }

}
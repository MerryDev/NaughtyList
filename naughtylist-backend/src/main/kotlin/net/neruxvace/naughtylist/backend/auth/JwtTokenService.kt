package net.neruxvace.naughtylist.backend.auth

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtTokenService(
    private val encoder: JwtEncoder,
    private val properties: JwtProperties
) {

    fun createToken(clientId: String): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(properties.issuer)
            .subject(clientId)
            .audience(listOf(properties.audience))
            .issuedAt(now)
            .expiresAt(now.plus(properties.ttl))
            .claim("client_type", "SERVER")
            .build()

        val header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build()

        return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

}
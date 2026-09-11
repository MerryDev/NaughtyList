package net.neruxvace.naughtylist.backend.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("naughtylist.security.jwt")
data class JwtProperties(
    var secret: String = "",
    var issuer: String = "",
    var audience: String = "",
    var ttl: Duration = Duration.ofMinutes(15)
)
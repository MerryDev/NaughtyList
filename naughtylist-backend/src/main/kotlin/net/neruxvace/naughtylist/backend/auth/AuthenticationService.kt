package net.neruxvace.naughtylist.backend.auth

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthenticationService(
    private val repository: ApiClientRepository,
    private val encoder: PasswordEncoder,
    private val service: JwtTokenService,
    private val properties: JwtProperties
) {

    fun authenticate(request: TokenRequest): TokenResponse {
        val client = repository.findById(request.clientId) ?: invalidCredentials()

        if (!client.enabled) invalidCredentials()
        if (!encoder.matches(request.clientSecret, client.secretHash)) invalidCredentials()

        return TokenResponse(
            accessToken = service.createToken(client.clientId),
            expiresIn = properties.ttl.seconds
        )
    }

    private fun invalidCredentials(): Nothing {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
    }

}
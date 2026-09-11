package net.neruxvace.naughtylist.backend.auth

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val authService: AuthenticationService,
    private val actorAuthService: ActorAuthenticationService,
    private val currentClient: CurrentClient
) {

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun createToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String
    ): OAuthTokenResponse {
        if (grantType != "client_credentials") throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported grant type")

        val response = authService.authenticate(TokenRequest(clientId, clientSecret))

        return OAuthTokenResponse(
            accessToken = response.accessToken,
            expiresIn = response.expiresIn
        )
    }

    @PostMapping("/actor-token")
    fun createActorToken(@Valid @RequestBody request: ActorTokenRequest): TokenResponse {
        return actorAuthService.createToken(request)
    }

    @GetMapping("/me")
    fun me(): ClientInfoResponse {
        val client = currentClient.requireServer()
        return ClientInfoResponse(clientId = client.clientId)
    }
}
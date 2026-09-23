package net.neruxvace.naughtylist.backend.auth

import org.springframework.stereotype.Service

@Service
class ActorAuthenticationService(
    private val currentClient: CurrentClient,
    private val service: JwtTokenService,
    private val properties: JwtProperties
) {

    fun createToken(request: ActorTokenRequest): TokenResponse {
        val client = currentClient.requireServer()
        val scopes = PermissionMapping.scopeFor(request.permissions)

        return TokenResponse(
            accessToken = service.createActorToken(
                playerUuid = request.playerUuid,
                clientId = client.clientId,
                scopes = scopes
            ),
            expiresIn = properties.ttl.seconds
        )
    }

}
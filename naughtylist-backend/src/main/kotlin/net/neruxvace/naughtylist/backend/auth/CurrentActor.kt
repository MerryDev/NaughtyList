package net.neruxvace.naughtylist.backend.auth

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Component
class CurrentActor {

    fun requireActor(): AuthenticatedActor {
        val authentication = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken ?: unauthorized()
        val token = authentication.token

        if (token.getClaimAsString("client_type") != "ACTOR") unauthorized()

        val clientId = token.getClaimAsString("client_id") ?: unauthorized()
        val playerUuid = try {
            UUID.fromString(token.subject)
        } catch (_: IllegalArgumentException) {
            unauthorized()
        }

        return AuthenticatedActor(playerUuid = playerUuid, clientId = clientId)
    }

    private fun unauthorized(): Nothing {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor authentication required")
    }
}
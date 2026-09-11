package net.neruxvace.naughtylist.backend.auth

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class CurrentClient {

    fun requireServer(): AuthenticatedClient {
        val authentication = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken ?: unauthorized()
        val token = authentication.token

        if (token.getClaimAsString("client_type") != "SERVER") unauthorized()
        return AuthenticatedClient(clientId = requireNotNull(token.subject) { "clientId is unexpectedly null" })
    }

    private fun unauthorized(): Nothing {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Server authentication required")
    }

}
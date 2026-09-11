package net.neruxvace.naughtylist.backend.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val service: AuthenticationService,
    private val currentClient: CurrentClient
) {

    @PostMapping("/token")
    fun createToken(@RequestBody request: TokenRequest): TokenResponse {
        return service.authenticate(request)
    }

    @GetMapping("/me")
    fun me(): ClientInfoResponse {
        val client = currentClient.requireServer()
        return ClientInfoResponse(clientId = client.clientId)
    }
}
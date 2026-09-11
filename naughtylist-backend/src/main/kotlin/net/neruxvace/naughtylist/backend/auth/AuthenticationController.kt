package net.neruxvace.naughtylist.backend.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(private val service: AuthenticationService) {

    @PostMapping("/token")
    fun createToken(@RequestBody request: TokenRequest): TokenResponse {
        return service.authenticate(request)
    }
}
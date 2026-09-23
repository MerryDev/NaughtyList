package net.neruxvace.naughtylist.backend.auth

data class TokenRequest(
    val clientId: String,
    val clientSecret: String
)

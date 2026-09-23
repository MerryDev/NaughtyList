package net.neruxvace.naughtylist.backend.auth

data class ApiClient(
    val clientId: String,
    val secretHash: String,
    val enabled: Boolean
)
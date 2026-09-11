package net.neruxvace.naughtylist.backend.auth

import java.util.UUID

data class AuthenticatedActor(
    val playerUuid: UUID,
    val clientId: String
)

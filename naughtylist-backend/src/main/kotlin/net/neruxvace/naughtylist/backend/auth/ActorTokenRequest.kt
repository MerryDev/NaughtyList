package net.neruxvace.naughtylist.backend.auth

import java.util.*

data class ActorTokenRequest(
    val playerUuid: UUID,
    val permissions: Set<String>
)

package net.neruxvace.naughtylist.backend.auth

import kotlin.uuid.Uuid

data class ActorTokenRequest(
    val playerUuid: Uuid,
    val permissions: Set<String>
)

package net.neruxvace.naughtylist.backend.auth

import kotlin.uuid.Uuid

data class AuthenticatedActor(
    val playerUuid: Uuid,
    val clientId: String
)

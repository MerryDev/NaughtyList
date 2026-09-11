package net.neruxvace.naughtylist.backend.auth

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ActorTokenRequest(
    val playerUuid: UUID,
    @NotEmpty val permissions: Set<String>
)

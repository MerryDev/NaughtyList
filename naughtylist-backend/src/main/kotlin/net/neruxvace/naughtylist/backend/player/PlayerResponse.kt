package net.neruxvace.naughtylist.backend.player

import java.time.LocalDateTime
import java.util.UUID

data class PlayerResponse(
    val uuid: UUID,
    val name: String?,
    val discordId: String?,
    val firstJoinedAt: LocalDateTime,
    val lastSeenAt: LocalDateTime?
)
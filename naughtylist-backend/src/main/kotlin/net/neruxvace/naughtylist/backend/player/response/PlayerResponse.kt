package net.neruxvace.naughtylist.backend.player.response

import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class PlayerResponse(
    val uuid: Uuid,
    val name: String?,
    val discordId: String?,
    val firstJoinedAt: LocalDateTime,
    val lastSeenAt: LocalDateTime?
)
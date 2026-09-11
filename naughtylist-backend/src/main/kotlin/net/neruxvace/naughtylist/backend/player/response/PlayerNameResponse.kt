package net.neruxvace.naughtylist.backend.player.response

import java.time.LocalDateTime

data class PlayerNameResponse(
    val name: String,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime?
)

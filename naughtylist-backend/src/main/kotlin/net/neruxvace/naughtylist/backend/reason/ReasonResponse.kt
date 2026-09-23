package net.neruxvace.naughtylist.backend.reason

import java.time.LocalDateTime

data class ReasonResponse(
    val id: Long,
    val key: String,
    val name: String,
    val description: String?,
    val enabled: Boolean,
    val createdAt: LocalDateTime
)

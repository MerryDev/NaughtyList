package net.neruxvace.naughtylist.backend.escalation

import java.time.LocalDateTime

data class EscalationPolicyResponse(
    val id: Long,
    val key: String,
    val name: String,
    val description: String?,
    val reasonId: Long?,
    val enabled: Boolean,
    val createdAt: LocalDateTime
)
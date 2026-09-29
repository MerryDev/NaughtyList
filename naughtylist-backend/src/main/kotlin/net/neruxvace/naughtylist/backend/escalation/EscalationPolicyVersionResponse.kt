package net.neruxvace.naughtylist.backend.escalation

import java.time.Duration
import java.time.LocalDateTime

data class EscalationPolicyVersionResponse(
    val id: Long,
    val policyId: Long,
    val version: Int,
    val lookback: Duration?,
    val active: Boolean,
    val steps: List<EscalationStepResponse>,
    val createdAt: LocalDateTime
)
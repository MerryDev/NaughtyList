package net.neruxvace.naughtylist.backend.escalation

data class EscalationRecommendationResponse(
    val id: Long,
    val policyVersionId: Long,
    val offenseNumber: Int,
    val step: EscalationStepResponse?
)
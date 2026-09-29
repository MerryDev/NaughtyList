package net.neruxvace.naughtylist.backend.escalation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateEscalationPolicyRequest(
    @field:NotBlank val key: String,
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:Positive val reasonId: Long? = null
)

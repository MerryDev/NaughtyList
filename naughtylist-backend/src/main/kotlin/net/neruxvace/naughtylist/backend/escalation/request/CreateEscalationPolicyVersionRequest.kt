package net.neruxvace.naughtylist.backend.escalation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import java.time.Duration

data class CreateEscalationPolicyVersionRequest(
    val lookback: Duration? = null,
    @field:NotEmpty
    @field:Valid
    val steps: List<CreateEscalationStepRequest>
)

data class CreateEscalationStepRequest(
    @field:Positive val minimumOffenses: Int,
    val type: PunishmentType,
    val duration: Duration? = null
)
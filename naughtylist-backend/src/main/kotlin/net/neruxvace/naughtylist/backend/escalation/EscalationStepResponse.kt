package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import java.time.Duration

data class EscalationStepResponse(
    val id: Long,
    val minimumOffenses: Int,
    val type: PunishmentType,
    val duration: Duration?
)

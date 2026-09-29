package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyVersionRequest
import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType

internal fun CreateEscalationPolicyVersionRequest.validateForCreation() {
    if (lookback != null && (lookback.isZero || lookback.isNegative)) {
        throw InvalidRequestException("Lookback must be positive")
    }

    if (steps.map { it.minimumOffenses }.distinct().size != steps.size) {
        throw InvalidRequestException("Minimum offenses must be unique")
    }

    steps.forEach { step ->
        if (step.duration != null && (step.duration.isZero || step.duration.isNegative)) {
            throw InvalidRequestException("Punishment duration must be positive")
        }
        if (step.type == PunishmentType.WARNING && step.duration != null) {
            throw InvalidRequestException("Warnings cannot have a duration")
        }
    }
}
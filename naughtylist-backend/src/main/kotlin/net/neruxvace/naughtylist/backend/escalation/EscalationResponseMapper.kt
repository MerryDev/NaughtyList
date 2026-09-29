package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationStepRecord
import net.neruxvace.naughtylist.backend.persistence.required

internal fun EscalationStepRecord.toResponse(): EscalationStepResponse =
    EscalationStepResponse(
        id = id.required(),
        minimumOffenses = minimumOffenses,
        type = type,
        duration = duration?.toDuration()
    )
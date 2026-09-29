package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationPolicyRecord
import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationPolicyVersionRecord
import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationStepRecord
import net.neruxvace.naughtylist.backend.persistence.required

internal fun EscalationStepRecord.toResponse(): EscalationStepResponse =
    EscalationStepResponse(
        id = id.required(),
        minimumOffenses = minimumOffenses,
        type = type,
        duration = duration?.toDuration()
    )

internal fun EscalationPolicyVersionRecord.toResponse(steps: List<EscalationStepResponse>): EscalationPolicyVersionResponse =
    EscalationPolicyVersionResponse(
        id = id.required(),
        policyId = policyId,
        version = version,
        lookback = lookback?.toDuration(),
        active = active.required(),
        steps = steps,
        createdAt = createdAt.required()
    )

internal fun EscalationPolicyRecord.toResponse(): EscalationPolicyResponse =
    EscalationPolicyResponse(
        id = id.required(),
        key = key,
        name = name,
        description = description,
        reasonId = reasonId,
        enabled = enabled.required(),
        createdAt = createdAt.required()
    )
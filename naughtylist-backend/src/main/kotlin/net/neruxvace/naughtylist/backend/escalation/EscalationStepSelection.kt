package net.neruxvace.naughtylist.backend.escalation

internal fun EscalationPolicyVersionResponse.stepFor(offenseNumber: Int): EscalationStepResponse? = steps
    .filter { it.minimumOffenses <= offenseNumber }
    .maxByOrNull { it.minimumOffenses }
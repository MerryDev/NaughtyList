package net.neruxvace.naughtylist.backend.moderation.evidence

import jakarta.validation.constraints.NotBlank
import net.neruxvace.naughtylist.backend.jooq.enums.EvidenceType

data class CreateCaseEvidenceRequest(val type: EvidenceType, @NotBlank val value: String)
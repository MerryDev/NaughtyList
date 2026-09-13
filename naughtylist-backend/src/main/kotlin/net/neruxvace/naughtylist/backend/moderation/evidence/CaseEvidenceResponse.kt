package net.neruxvace.naughtylist.backend.moderation.evidence

import net.neruxvace.naughtylist.backend.jooq.enums.EvidenceType
import java.time.LocalDateTime
import java.util.UUID

data class CaseEvidenceResponse(
    val id: Long,
    val caseId: Long,
    val addedBy: UUID,
    val type: EvidenceType,
    val value: String,
    val createdAt: LocalDateTime
)

package net.neruxvace.naughtylist.backend.moderation.evidence

import net.neruxvace.naughtylist.backend.jooq.enums.EvidenceType
import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class CaseEvidenceResponse(
    val id: Long,
    val caseId: Long,
    val addedBy: Uuid,
    val type: EvidenceType,
    val value: String,
    val createdAt: LocalDateTime
)

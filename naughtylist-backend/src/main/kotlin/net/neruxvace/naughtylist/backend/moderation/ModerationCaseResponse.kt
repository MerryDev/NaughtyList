package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import java.time.LocalDateTime
import java.util.UUID

data class ModerationCaseResponse(
    val id: Long,
    val targetUuid: UUID,
    val status: CaseStatus,
    val title: String?,
    val summary: String?,
    val createdBy: UUID?,
    val assignedTo: UUID?,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime?
)

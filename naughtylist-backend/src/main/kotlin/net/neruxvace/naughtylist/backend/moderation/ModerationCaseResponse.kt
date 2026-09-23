package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class ModerationCaseResponse(
    val id: Long,
    val targetUuid: Uuid,
    val status: CaseStatus,
    val title: String?,
    val summary: String?,
    val createdBy: Uuid?,
    val assignedTo: Uuid?,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime?
)

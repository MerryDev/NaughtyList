package net.neruxvace.naughtylist.backend.report

import net.neruxvace.naughtylist.backend.jooq.enums.ReportStatus
import java.time.LocalDateTime
import java.util.UUID

data class ReportResponse(
    val id: Long,
    val replayId: String?,
    val serverName: String,
    val reporterUuid: UUID,
    val targetUuid: UUID,
    val reasonId: Long,
    val status: ReportStatus,
    val caseId: Long?,
    val createdAt: LocalDateTime
)

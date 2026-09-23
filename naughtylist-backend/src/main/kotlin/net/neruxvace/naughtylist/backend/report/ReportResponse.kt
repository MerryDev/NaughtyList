package net.neruxvace.naughtylist.backend.report

import net.neruxvace.naughtylist.backend.jooq.enums.ReportStatus
import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class ReportResponse(
    val id: Long,
    val replayId: String?,
    val serverName: String,
    val reporterUuid: Uuid,
    val targetUuid: Uuid,
    val reasonId: Long,
    val status: ReportStatus,
    val caseId: Long?,
    val createdAt: LocalDateTime
)

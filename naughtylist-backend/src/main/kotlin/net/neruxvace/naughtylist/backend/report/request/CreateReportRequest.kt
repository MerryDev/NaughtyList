package net.neruxvace.naughtylist.backend.report.request

import java.util.UUID

data class CreateReportRequest(
    val reporterUuid: UUID,
    val targetUuid: UUID,
    val reasonId: Long,
    val replayId: String? = null
)

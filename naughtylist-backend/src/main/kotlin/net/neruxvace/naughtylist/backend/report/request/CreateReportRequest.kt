package net.neruxvace.naughtylist.backend.report.request

import kotlin.uuid.Uuid

data class CreateReportRequest(
    val reporterUuid: Uuid,
    val targetUuid: Uuid,
    val reasonId: Long,
    val replayId: String? = null
)

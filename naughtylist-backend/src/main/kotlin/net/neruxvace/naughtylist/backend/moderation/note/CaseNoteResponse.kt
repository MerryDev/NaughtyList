package net.neruxvace.naughtylist.backend.moderation.note

import java.time.LocalDateTime
import java.util.UUID

data class CaseNoteResponse(
    val id: Long,
    val caseId: Long,
    val authorUuid: UUID,
    val content: String,
    val createdAt: LocalDateTime
)
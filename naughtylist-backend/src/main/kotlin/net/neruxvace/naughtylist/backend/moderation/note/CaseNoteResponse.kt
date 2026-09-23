package net.neruxvace.naughtylist.backend.moderation.note

import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class CaseNoteResponse(
    val id: Long,
    val caseId: Long,
    val authorUuid: Uuid,
    val content: String,
    val createdAt: LocalDateTime
)
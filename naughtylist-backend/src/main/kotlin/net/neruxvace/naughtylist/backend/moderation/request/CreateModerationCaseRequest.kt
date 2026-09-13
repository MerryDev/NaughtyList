package net.neruxvace.naughtylist.backend.moderation.request

import java.util.UUID

data class CreateModerationCaseRequest(
    val targetUuid: UUID,
    val title: String? = null,
    val summary: String? = null,
    val assignedTo: UUID? = null
)

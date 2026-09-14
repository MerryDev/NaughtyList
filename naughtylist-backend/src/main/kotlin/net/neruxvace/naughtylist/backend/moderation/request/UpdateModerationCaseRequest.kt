package net.neruxvace.naughtylist.backend.moderation.request

import java.util.UUID

data class UpdateModerationCaseRequest(
    val title: String? = null,
    val summary: String? = null,
    val assignedTo: UUID? = null
)

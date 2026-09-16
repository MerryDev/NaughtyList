package net.neruxvace.naughtylist.backend.moderation.request

import kotlin.uuid.Uuid

data class UpdateModerationCaseRequest(
    val title: String? = null,
    val summary: String? = null,
    val assignedTo: Uuid? = null
)

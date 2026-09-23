package net.neruxvace.naughtylist.backend.moderation.request

import kotlin.uuid.Uuid

data class CreateModerationCaseRequest(
    val targetUuid: Uuid,
    val title: String? = null,
    val summary: String? = null,
    val assignedTo: Uuid? = null
)

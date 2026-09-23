package net.neruxvace.naughtylist.backend.punishment.request

import jakarta.validation.constraints.Positive
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class CreatePunishmentRequest(
    val type: PunishmentType,
    val playerUuid: Uuid,
    @Positive val reasonId: Long,
    @Positive val caseId: Long? = null,
    val startsAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null
)

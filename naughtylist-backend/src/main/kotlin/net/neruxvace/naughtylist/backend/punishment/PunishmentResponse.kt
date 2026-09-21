package net.neruxvace.naughtylist.backend.punishment

import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import java.time.LocalDateTime
import kotlin.uuid.Uuid

data class PunishmentResponse(
    val id: Long,
    val type: PunishmentType,
    val playerUuid: Uuid,
    val reasonId: Long,
    val caseId: Long?,
    val issuedBy: Uuid,
    val escalationPolicyVersionId: Long?,
    val escalationStepId: Long?,
    val startsAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val revokedAt: LocalDateTime?,
    val revokedBy: Uuid?,
    val revokeReason: String?,
    val overrideReason: String?,
    val createdAt: LocalDateTime
)

package net.neruxvace.naughtylist.backend.punishment

import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import net.neruxvace.naughtylist.backend.jooq.tables.records.PunishmentRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.PUNISHMENT
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.uuid.Uuid

@Service
class PunishmentService(private val context: DSLContext) {

    fun findAll(playerUuid: Uuid?, type: PunishmentType?, reasonId: Long?, caseId: Long?, active: Boolean?): List<PunishmentResponse> {
        var condition: Condition = DSL.trueCondition()

        playerUuid?.let { condition = condition.and(PUNISHMENT.PLAYER_UUID.eq(it)) }
        type?.let { condition = condition.and(PUNISHMENT.TYPE.eq(it)) }
        reasonId?.let { condition = condition.and(PUNISHMENT.REASON_ID.eq(it)) }
        caseId?.let { condition = condition.and(PUNISHMENT.CASE_ID.eq(it)) }
        active?.let {
            val now = LocalDateTime.now()
            val activeCondition = PUNISHMENT.REVOKED_AT.isNull
                .and(PUNISHMENT.STARTS_AT.le(now))
                .and(PUNISHMENT.EXPIRES_AT.isNull.or(PUNISHMENT.EXPIRES_AT.gt(now)))

            condition = condition.and(if (it) activeCondition else activeCondition.not())
        }

        return context
            .selectFrom(PUNISHMENT)
            .where(condition)
            .orderBy(PUNISHMENT.CREATED_AT.desc())
            .fetch().map(::map)
    }

    fun findById(id: Long): PunishmentResponse? = context
        .selectFrom(PUNISHMENT)
        .where(PUNISHMENT.ID.eq(id))
        .fetchOne()?.let(::map)

    private fun map(record: PunishmentRecord): PunishmentResponse =
        PunishmentResponse(
            id = record.id.required(),
            type = record.type,
            playerUuid = record.playerUuid,
            reasonId = record.reasonId,
            caseId = record.caseId,
            issuedBy = record.issuedBy,
            escalationPolicyVersionId = record.escalationPolicyVersionId,
            escalationStepId = record.escalationStepId,
            startsAt = record.startsAt.required(),
            expiresAt = record.expiresAt,
            revokedAt = record.revokedAt,
            revokedBy = record.revokedBy,
            revokeReason = record.revokeReason,
            overrideReason = record.overrideReason,
            createdAt = record.createdAt.required()
        )
}
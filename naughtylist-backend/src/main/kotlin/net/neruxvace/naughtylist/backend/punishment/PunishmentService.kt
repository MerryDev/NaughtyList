package net.neruxvace.naughtylist.backend.punishment

import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import net.neruxvace.naughtylist.backend.jooq.tables.records.PunishmentRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.jooq.tables.references.PUNISHMENT
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.punishment.request.CreatePunishmentRequest
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
            val activeCondition = PUNISHMENT.TYPE
                .`in`(PunishmentType.MUTE, PunishmentType.BAN)
                .and(PUNISHMENT.REVOKED_AT.isNull)
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


    @Transactional
    fun create(request: CreatePunishmentRequest, actorUuid: Uuid): PunishmentResponse {
        val startsAt = request.startsAt ?: LocalDateTime.now()

        if (request.expiresAt != null && !request.expiresAt.isAfter(startsAt)) {
            throw InvalidRequestException("The end of a punishment must be after its start")
        }

        requirePlayer(request.playerUuid)
        requireEnabledReason(request.reasonId)
        request.caseId?.let { requireOpenCase(it, request.playerUuid) }

        val record = context
            .insertInto(PUNISHMENT)
            .set(PUNISHMENT.TYPE, request.type)
            .set(PUNISHMENT.PLAYER_UUID, request.playerUuid)
            .set(PUNISHMENT.REASON_ID, request.reasonId)
            .set(PUNISHMENT.CASE_ID, request.caseId)
            .set(PUNISHMENT.ISSUED_BY, actorUuid)
            .set(PUNISHMENT.STARTS_AT, startsAt)
            .set(PUNISHMENT.EXPIRES_AT, request.expiresAt)
            .returning()
            .fetchOne() ?: error("Failed to create punishment")

        return map(record)
    }

    private fun requirePlayer(uuid: Uuid) {
        val exists = context.fetchExists(
            context.selectOne()
                .from(PLAYER)
                .where(PLAYER.UUID.eq(uuid))
        )
        if (!exists) throw ResourceNotFoundException("Player not found")
    }

    private fun requireEnabledReason(id: Long) {
        val reason = context
            .select(REASON.ENABLED)
            .from(REASON)
            .where(REASON.ID.eq(id))
            .fetchOne() ?: throw ResourceNotFoundException("Reason not found")

        if (reason.value1() != true) throw InvalidRequestException("Reason is disabled")
    }

    private fun requireOpenCase(id: Long, playerUuid: Uuid) {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(id))
            .forUpdate()
            .fetchOne() ?: throw ResourceNotFoundException("Moderation case not found")

        if (case.targetUuid != playerUuid) {
            throw ResourceConflictException("Moderation case target does not match punishment target")
        }
        if (case.status != CaseStatus.OPEN) {
            throw ResourceConflictException("Moderation case is not open")
        }
    }

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
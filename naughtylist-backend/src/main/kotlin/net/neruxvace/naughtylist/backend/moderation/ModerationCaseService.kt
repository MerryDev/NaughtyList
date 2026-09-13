package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.tables.records.ModerationCaseRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import net.neruxvace.naughtylist.backend.moderation.request.UpdateModerationCaseRequest
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ModerationCaseService(private val context: DSLContext) {

    fun findAll(status: CaseStatus?, targetUuid: UUID?, assignedTo: UUID?): List<ModerationCaseResponse> {
        var condition: Condition = DSL.trueCondition()

        status?.let { condition = condition.and(MODERATION_CASE.STATUS.eq(it)) }
        targetUuid?.let { condition = condition.and(MODERATION_CASE.TARGET_UUID.eq(it)) }
        assignedTo?.let { condition = condition.and(MODERATION_CASE.ASSIGNED_TO.eq(it)) }

        return context
            .selectFrom(MODERATION_CASE)
            .where(condition)
            .orderBy(MODERATION_CASE.CREATED_AT.desc())
            .fetch().map(::map)
    }

    fun findById(id: Long): ModerationCaseResponse? {
        return context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(id))
            .fetchOne()?.let(::map)
    }

    fun create(request: CreateModerationCaseRequest, actorUuid: UUID): ModerationCaseResponse {
        requirePlayer(request.targetUuid, "Target player not found")
        request.assignedTo?.let { requirePlayer(it, "Assigned player not found") }

        val record = context
            .insertInto(MODERATION_CASE)
            .set(MODERATION_CASE.TARGET_UUID, request.targetUuid)
            .set(MODERATION_CASE.TITLE, request.title)
            .set(MODERATION_CASE.SUMMARY, request.summary)
            .set(MODERATION_CASE.CREATED_BY, actorUuid)
            .set(MODERATION_CASE.ASSIGNED_TO, request.assignedTo)
            .returning()
            .fetchOne() ?: error("Failed to create moderation case")

        return map(record)
    }

    fun update(id: Long, request: UpdateModerationCaseRequest): ModerationCaseResponse? {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(id))
            .fetchOne() ?: return null

        if (case.status != CaseStatus.OPEN) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Moderation case is not open")
        }

        request.assignedTo?.let { requirePlayer(it, "Assigned player not found") }

        val hasChanges = request.title != null || request.summary != null || request.assignedTo != null
        if (!hasChanges) return map(case)

        val update = context.updateQuery(MODERATION_CASE)
        request.title?.let { update.addValue(MODERATION_CASE.TITLE, it) }
        request.summary?.let { update.addValue(MODERATION_CASE.SUMMARY, it) }
        request.assignedTo?.let { update.addValue(MODERATION_CASE.ASSIGNED_TO, it) }

        update.addConditions(MODERATION_CASE.ID.eq(id))
        update.execute()

        return findById(id) ?: error("Moderation case disappeared after update")
    }


    private fun requirePlayer(uuid: UUID, message: String) {
        val exists = context.fetchExists(
            context.selectOne()
                .from(PLAYER)
                .where(PLAYER.UUID.eq(uuid))
        )
        if (!exists) throw ResponseStatusException(HttpStatus.NOT_FOUND, message)
    }

    private fun map(record: ModerationCaseRecord): ModerationCaseResponse =
        ModerationCaseResponse(
            id = requireNotNull(record.id),
            targetUuid = record.targetUuid,
            status = requireNotNull(record.status),
            title = record.title,
            summary = record.summary,
            createdBy = record.createdBy,
            assignedTo = record.assignedTo,
            createdAt = requireNotNull(record.createdAt),
            closedAt = record.closedAt
        )
}
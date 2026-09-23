package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.enums.ReportStatus
import net.neruxvace.naughtylist.backend.jooq.tables.records.ModerationCaseRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.jooq.tables.references.REPORT
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import net.neruxvace.naughtylist.backend.moderation.request.UpdateModerationCaseRequest
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.web.ifPresent
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.uuid.Uuid

@Service
class ModerationCaseService(private val context: DSLContext) {

    fun findAll(status: CaseStatus?, targetUuid: Uuid?, assignedTo: Uuid?): List<ModerationCaseResponse> {
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

    fun findById(id: Long): ModerationCaseResponse? = context
        .selectFrom(MODERATION_CASE)
        .where(MODERATION_CASE.ID.eq(id))
        .fetchOne()?.let(::map)

    fun create(request: CreateModerationCaseRequest, actorUuid: Uuid): ModerationCaseResponse {
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
            throw ResourceConflictException("Moderation case is not open")
        }
        if (request.isEmpty()) return map(case)

        request.assignedTo.ifPresent { assignee ->
            assignee?.let { requirePlayer(it, "Assigned player not found") }
        }

        val update = context.updateQuery(MODERATION_CASE)

        request.title.ifPresent {
            update.addValue(
                MODERATION_CASE.TITLE,
                it ?: throw InvalidRequestException("Title cannot be null")
            )
        }
        request.summary.ifPresent { update.addValue(MODERATION_CASE.SUMMARY, it) }
        request.assignedTo.ifPresent { update.addValue(MODERATION_CASE.ASSIGNED_TO, it) }

        update.addConditions(MODERATION_CASE.ID.eq(id))
        update.execute()

        return findById(id) ?: error("Moderation case disappeared after update")
    }

    @Transactional
    fun close(id: Long): ModerationCaseResponse? = updateStatus(id, CaseStatus.CLOSED)

    @Transactional
    fun dismiss(id: Long): ModerationCaseResponse? = updateStatus(id, CaseStatus.DISMISSED)

    private fun updateStatus(id: Long, status: CaseStatus): ModerationCaseResponse? {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(id))
            .forUpdate()
            .fetchOne() ?: return null

        if (case.status != CaseStatus.OPEN) {
            throw ResourceConflictException("Moderation case is not open")
        }

        requireNoOpenReports(id)

        val updated = context
            .update(MODERATION_CASE)
            .set(MODERATION_CASE.STATUS, status)
            .set(MODERATION_CASE.CLOSED_AT, LocalDateTime.now())
            .where(MODERATION_CASE.ID.eq(id))
            .returning()
            .fetchOne() ?: error("Failed to update moderation case status")

        return map(updated)
    }

    private fun requireNoOpenReports(caseId: Long) {
        val hasOpenReports = context.fetchExists(
            context.selectOne()
                .from(REPORT)
                .where(REPORT.CASE_ID.eq(caseId))
                .and(REPORT.STATUS.eq(ReportStatus.OPEN))
        )

        if (hasOpenReports) {
            throw ResourceConflictException("Moderation case has open reports")
        }
    }

    private fun requirePlayer(uuid: Uuid, message: String) {
        val exists = context.fetchExists(
            context.selectOne()
                .from(PLAYER)
                .where(PLAYER.UUID.eq(uuid))
        )
        if (!exists) throw ResourceNotFoundException(message)
    }

    private fun map(record: ModerationCaseRecord): ModerationCaseResponse =
        ModerationCaseResponse(
            id = record.id.required(),
            targetUuid = record.targetUuid,
            status = record.status.required(),
            title = record.title,
            summary = record.summary,
            createdBy = record.createdBy,
            assignedTo = record.assignedTo,
            createdAt = record.createdAt.required(),
            closedAt = record.closedAt
        )
}
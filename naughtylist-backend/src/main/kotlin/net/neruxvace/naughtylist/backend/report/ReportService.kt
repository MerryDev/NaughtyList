package net.neruxvace.naughtylist.backend.report

import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.enums.ReportStatus
import net.neruxvace.naughtylist.backend.jooq.tables.records.ReportRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.jooq.tables.references.REPORT
import net.neruxvace.naughtylist.backend.moderation.ModerationCaseService
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.report.request.CreateReportRequest
import net.neruxvace.naughtylist.backend.report.request.UpdateReportCaseRequest
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.Uuid

@Service
class ReportService(
    private val context: DSLContext,
    private val moderationCaseService: ModerationCaseService
) {

    fun findAll(status: ReportStatus?, targetUuid: Uuid?, serverName: String?, caseId: Long?): List<ReportResponse> {
        var condition: Condition = DSL.trueCondition()

        status?.let { condition = condition.and(REPORT.STATUS.eq(it)) }
        targetUuid?.let { condition = condition.and(REPORT.TARGET_UUID.eq(it)) }
        serverName?.let { condition = condition.and(REPORT.SERVER_NAME.eq(it)) }
        caseId?.let { condition = condition.and(REPORT.CASE_ID.eq(it)) }

        return context
            .selectFrom(REPORT)
            .where(condition)
            .fetch()
            .map { map(it) }
    }

    fun findById(id: Long): ReportResponse? {
        return findReport(id)?.let { map(it) }
    }

    fun create(request: CreateReportRequest, serverName: String): ReportResponse {
        requirePlayer(request.reporterUuid)
        requirePlayer(request.targetUuid)
        requireEnabledReason(request.reasonId)

        val record = context
            .insertInto(REPORT)
            .set(REPORT.REPLAY_ID, request.replayId)
            .set(REPORT.SERVER_NAME, serverName)
            .set(REPORT.REPORTER_UUID, request.reporterUuid)
            .set(REPORT.TARGET_UUID, request.targetUuid)
            .set(REPORT.REASON_ID, request.reasonId)
            .returning()
            .fetchOne() ?: error("Failed to create report")

        return map(record)
    }

    @Transactional
    fun close(id: Long): ReportResponse? {
        val report = findReportForUpdate(id) ?: return null

        if (report.status != ReportStatus.OPEN) {
            throw ResourceConflictException("Report is not open")
        }

        val updated = context
            .update(REPORT)
            .set(REPORT.STATUS, ReportStatus.CLOSED)
            .where(REPORT.ID.eq(id))
            .returning()
            .fetchOne() ?: error("Failed to close report")

        return map(updated)
    }

    @Transactional
    fun accept(id: Long, actorUuid: Uuid): ReportResponse? {
        val report = findReportForUpdate(id) ?: return null

        if (report.status != ReportStatus.OPEN) {
            throw ResourceConflictException("Report is not open")
        }

        val caseId = report.caseId?.let { caseId ->
            requireOpenCase(caseId)
            caseId
        } ?: resolveCaseForAcceptance(report.targetUuid, actorUuid)

        val updated = context
            .update(REPORT)
            .set(REPORT.STATUS, ReportStatus.ACCEPTED)
            .set(REPORT.CASE_ID, caseId)
            .where(REPORT.ID.eq(id))
            .returning()
            .fetchOne() ?: error("Failed to accept report")

        return map(updated)
    }

    @Transactional
    fun updateCase(id: Long, request: UpdateReportCaseRequest): ReportResponse? {
        val report = findReportForUpdate(id) ?: return null

        if (report.status != ReportStatus.OPEN) {
            throw ResourceConflictException("Report is not open")
        }

        request.caseId?.let { caseId ->
            val case = context
                .selectFrom(MODERATION_CASE)
                .where(MODERATION_CASE.ID.eq(caseId))
                .fetchOne() ?: throw ResourceNotFoundException("Moderation case not found")

            if (case.status != CaseStatus.OPEN) {
                throw ResourceConflictException("Moderation case is not open")
            }
            if (case.targetUuid != report.targetUuid) {
                throw ResourceConflictException("Moderation case target does not match report target")
            }
        }

        val updated = context
            .update(REPORT)
            .set(REPORT.CASE_ID, request.caseId)
            .where(REPORT.ID.eq(id))
            .returning()
            .fetchOne() ?: error("Failed to update report case")

        return map(updated)
    }

    private fun map(record: ReportRecord): ReportResponse {
        return ReportResponse(
            id = record.id.required(),
            replayId = record.replayId,
            serverName = record.serverName,
            reporterUuid = record.reporterUuid,
            targetUuid = record.targetUuid,
            reasonId = record.reasonId,
            status = record.status.required(),
            caseId = record.caseId,
            createdAt = record.createdAt.required()
        )
    }

    private fun findReportForUpdate(id: Long): ReportRecord? {
        return context
            .selectFrom(REPORT)
            .where(REPORT.ID.eq(id))
            .forUpdate()
            .fetchOne()
    }

    private fun requireOpenCase(id: Long) {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(id))
            .forUpdate()
            .fetchOne() ?: throw ResourceNotFoundException("Moderation case not found")

        if (case.status != CaseStatus.OPEN) throw ResourceConflictException("Moderation case is not open")
    }

    private fun resolveCaseForAcceptance(targetUuid: Uuid, actorUuid: Uuid): Long {
        lockPlayer(targetUuid)

        val openCases = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.TARGET_UUID.eq(targetUuid))
            .and(MODERATION_CASE.STATUS.eq(CaseStatus.OPEN))
            .limit(2)
            .fetch()

        return when (openCases.size) {
            0 -> moderationCaseService.create(CreateModerationCaseRequest(targetUuid), actorUuid).id
            1 -> openCases.single().id.required()
            else -> throw ResourceConflictException("Multiple open moderation cases exist; assign the report to a case before accepting it")
        }
    }

    private fun lockPlayer(uuid: Uuid) {
        context
            .select(PLAYER.UUID)
            .from(PLAYER)
            .where(PLAYER.UUID.eq(uuid))
            .forUpdate()
            .fetchOne() ?: error("Report target player does not exist")
    }

    private fun findReport(id: Long): ReportRecord? {
        return context
            .selectFrom(REPORT)
            .where(REPORT.ID.eq(id))
            .fetchOne()
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
}
package net.neruxvace.naughtylist.backend.report

import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.jooq.tables.references.REPORT
import net.neruxvace.naughtylist.backend.report.request.CreateReportRequest
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ReportService(private val context: DSLContext) {

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

        return ReportResponse(
            id = requireNotNull(record.id),
            replayId = record.replayId,
            serverName = record.serverName,
            reporterUuid = record.reporterUuid,
            targetUuid = record.targetUuid,
            reasonId = record.reasonId,
            status = requireNotNull(record.status),
            caseId = record.caseId,
            createdAt = requireNotNull(record.createdAt),
        )
    }

    private fun requirePlayer(uuid: UUID) {
        val exists = context.fetchExists(
            context.selectOne()
                .from(PLAYER)
                .where(PLAYER.UUID.eq(uuid))
        )
        if (!exists) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")
    }

    private fun requireEnabledReason(id: Long) {
        val reason = context
            .select(REASON.ENABLED)
            .from(REASON)
            .where(REASON.ID.eq(id))
            .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reason not found")

        if (reason.value1() != true) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is disabled")
    }
}
package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.jooq.tables.records.ModerationCaseRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ModerationCaseService(private val context: DSLContext) {

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
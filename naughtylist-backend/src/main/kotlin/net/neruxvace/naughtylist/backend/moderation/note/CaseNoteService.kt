package net.neruxvace.naughtylist.backend.moderation.note

import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.tables.records.CaseNoteRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.CASE_NOTE
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.moderation.note.request.CreateCaseNoteRequest
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class CaseNoteService(private val context: DSLContext) {

    fun findAllByCaseId(caseId: Long): List<CaseNoteResponse> {
        val caseExists = context.fetchExists(
            context
                .selectOne()
                .from(MODERATION_CASE)
                .where(MODERATION_CASE.ID.eq(caseId))
        )
        if (!caseExists) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
        }

        return context
            .selectFrom(CASE_NOTE)
            .where(CASE_NOTE.CASE_ID.eq(caseId))
            .orderBy(CASE_NOTE.CREATED_AT.asc())
            .fetch().map(::map)
    }

    fun create(caseId: Long, request: CreateCaseNoteRequest, actorUuid: UUID): CaseNoteResponse {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(caseId))
            .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")

        if (case.status != CaseStatus.OPEN) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Moderation case is not open")
        }

        val record = context
            .insertInto(CASE_NOTE)
            .set(CASE_NOTE.CASE_ID, caseId)
            .set(CASE_NOTE.AUTHOR_UUID, actorUuid)
            .set(CASE_NOTE.CONTENT, request.content.trim())
            .returning()
            .fetchOne() ?: error("Failed to create case note")

        return map(record)
    }

    private fun map(record: CaseNoteRecord): CaseNoteResponse =
        CaseNoteResponse(
            id = requireNotNull(record.id),
            caseId = record.caseId,
            authorUuid = record.authorUuid,
            content = record.content,
            createdAt = requireNotNull(record.createdAt),
        )
}
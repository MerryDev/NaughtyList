package net.neruxvace.naughtylist.backend.moderation.evidence

import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.jooq.tables.records.CaseEvidenceRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.CASE_EVIDENCE
import net.neruxvace.naughtylist.backend.jooq.tables.references.MODERATION_CASE
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import kotlin.uuid.Uuid

@Service
class CaseEvidenceService(private val context: DSLContext) {

    fun findAllByCaseId(caseId: Long): List<CaseEvidenceResponse> {
        val exists = context.fetchExists(
            context
                .selectOne()
                .from(MODERATION_CASE)
                .where(MODERATION_CASE.ID.eq(caseId))
        )

        if (!exists) throw ResourceNotFoundException("Moderation case not found")

        return context
            .selectFrom(CASE_EVIDENCE)
            .where(CASE_EVIDENCE.CASE_ID.eq(caseId))
            .orderBy(CASE_EVIDENCE.CREATED_AT.asc())
            .fetch().map(::map)
    }

    fun create(caseId: Long, request: CreateCaseEvidenceRequest, actorUuid: Uuid): CaseEvidenceResponse {
        val case = context
            .selectFrom(MODERATION_CASE)
            .where(MODERATION_CASE.ID.eq(caseId))
            .fetchOne() ?: throw ResourceNotFoundException("Moderation case not found")

        if (case.status != CaseStatus.OPEN) {
            throw ResourceConflictException("Moderation case is not open")
        }

        val record = context
            .insertInto(CASE_EVIDENCE)
            .set(CASE_EVIDENCE.CASE_ID, caseId)
            .set(CASE_EVIDENCE.ADDED_BY, actorUuid)
            .set(CASE_EVIDENCE.TYPE, request.type)
            .set(CASE_EVIDENCE.VALUE, request.value.trim())
            .returning()
            .fetchOne() ?: error("Failed to create case evidence")

        return map(record)
    }

    private fun map(record: CaseEvidenceRecord): CaseEvidenceResponse =
        CaseEvidenceResponse(
            id = record.id.required(),
            caseId = record.caseId,
            addedBy = record.addedBy,
            type = record.type,
            value = record.value,
            createdAt = record.createdAt.required()
        )
}
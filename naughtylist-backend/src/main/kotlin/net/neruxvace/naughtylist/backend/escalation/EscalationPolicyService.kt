package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationPolicyRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EscalationPolicyService(private val context: DSLContext) {

    fun findAll(): List<EscalationPolicyResponse> = context
        .selectFrom(ESCALATION_POLICY)
        .orderBy(ESCALATION_POLICY.NAME.asc(), ESCALATION_POLICY.ID.asc())
        .fetch().map { it.toResponse() }
    fun findById(id: Long): EscalationPolicyResponse? = context
        .selectFrom(ESCALATION_POLICY)
        .where(ESCALATION_POLICY.ID.eq(id))
        .fetchOne()?.toResponse()

    fun create(request: CreateEscalationPolicyRequest): EscalationPolicyResponse {
        request.reasonId?.let { reasonId ->
            val reasonExists = context.fetchExists(
                context.selectOne()
                    .from(REASON)
                    .where(REASON.ID.eq(reasonId))
            )
            if (!reasonExists) throw ResourceNotFoundException("Reason with id $reasonId not found")
        }

        val record = context
            .insertInto(ESCALATION_POLICY)
            .set(ESCALATION_POLICY.KEY, request.key)
            .set(ESCALATION_POLICY.NAME, request.name)
            .set(ESCALATION_POLICY.DESCRIPTION, request.description)
            .set(ESCALATION_POLICY.REASON_ID, request.reasonId)
            .onConflict(ESCALATION_POLICY.KEY).doNothing()
            .returning()
            .fetchOne() ?: throw ResourceConflictException("An escalation policy with key ${request.key} already exists")

        return record.toResponse()
    }
}
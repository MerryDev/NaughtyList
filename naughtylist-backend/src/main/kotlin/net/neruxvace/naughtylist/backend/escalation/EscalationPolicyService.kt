package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.tables.records.EscalationPolicyRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EscalationPolicyService(private val context: DSLContext) {

    fun findAll(): List<EscalationPolicyResponse> = context
        .selectFrom(ESCALATION_POLICY)
        .orderBy(ESCALATION_POLICY.NAME.asc(), ESCALATION_POLICY.ID.asc())
        .fetch().map(::map)

    fun findById(id: Long): EscalationPolicyResponse? = context
        .selectFrom(ESCALATION_POLICY)
        .where(ESCALATION_POLICY.ID.eq(id))
        .fetchOne()?.let(::map)

    private fun map(record: EscalationPolicyRecord): EscalationPolicyResponse =
        EscalationPolicyResponse(
            id = record.id.required(),
            key = record.key,
            name = record.name,
            description = record.description,
            reasonId = record.reasonId,
            enabled = record.enabled.required(),
            createdAt = record.createdAt.required()
        )
}
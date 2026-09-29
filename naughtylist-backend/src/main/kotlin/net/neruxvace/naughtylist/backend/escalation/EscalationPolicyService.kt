package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.escalation.request.UpdateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.web.ifPresent
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    fun update(id: Long, request: UpdateEscalationPolicyRequest): EscalationPolicyResponse? {
        val policy = context
            .selectFrom(ESCALATION_POLICY)
            .where(ESCALATION_POLICY.ID.eq(id))
            .forUpdate()
            .fetchOne() ?: return null

        if (request.isEmpty()) return policy.toResponse()

        val update = context.updateQuery(ESCALATION_POLICY)

        request.name.ifPresent { name ->
            update.addValue(
                ESCALATION_POLICY.NAME,
                name?.takeIf { it.isNotBlank() } ?: throw InvalidRequestException("Name cannot be null or blank")
            )
        }
        request.description.ifPresent { update.addValue(ESCALATION_POLICY.DESCRIPTION, it) }
        request.enabled.ifPresent {
            update.addValue(
                ESCALATION_POLICY.ENABLED,
                it ?: throw InvalidRequestException("Enabled cannot be null")
            )
        }

        update.addConditions(ESCALATION_POLICY.ID.eq(id))
        update.execute()

        return findById(id)
    }
}
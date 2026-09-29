package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY_VERSION
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_STEP
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EscalationPolicyVersionService(private val context: DSLContext) {

    fun findAll(policyId: Long): List<EscalationPolicyVersionResponse> {
        val policyExists = context.fetchExists(
            context.selectOne()
                .from(ESCALATION_POLICY)
                .where(ESCALATION_POLICY.ID.eq(policyId))
        )
        if (!policyExists) throw ResourceNotFoundException("Escalation policy not found")

        val versions = context
            .selectFrom(ESCALATION_POLICY_VERSION)
            .where(ESCALATION_POLICY_VERSION.POLICY_ID.eq(policyId))
            .orderBy(ESCALATION_POLICY_VERSION.VERSION.desc())
            .fetch()

        if (versions.isEmpty()) return emptyList()

        val stepsByVersion = context
            .selectFrom(ESCALATION_STEP)
            .where(
                ESCALATION_STEP.POLICY_VERSION_ID.`in`(
                    versions.map { it.id.required() }
                )
            )
            .orderBy(ESCALATION_STEP.MINIMUM_OFFENSES.asc())
            .fetch()
            .groupBy { it.policyVersionId }

        return versions.map { version ->
            val steps = stepsByVersion[version.id.required()].orEmpty().map { it.toResponse() }
            version.toResponse(steps)
        }
    }

    fun findById(policyId: Long, versionId: Long): EscalationPolicyVersionResponse? {
        val version = context
            .selectFrom(ESCALATION_POLICY_VERSION)
            .where(
                ESCALATION_POLICY_VERSION.ID
                    .eq(versionId)
                    .and(ESCALATION_POLICY_VERSION.POLICY_ID.eq(policyId))
            )
            .fetchOne() ?: return null

        val steps = context
            .selectFrom(ESCALATION_STEP)
            .where(ESCALATION_STEP.POLICY_VERSION_ID.eq(versionId))
            .orderBy(ESCALATION_STEP.MINIMUM_OFFENSES.asc())
            .fetch().map { it.toResponse() }

        return version.toResponse(steps)
    }

}
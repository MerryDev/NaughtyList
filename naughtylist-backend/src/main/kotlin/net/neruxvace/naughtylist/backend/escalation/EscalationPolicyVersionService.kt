package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY_VERSION
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_STEP
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class EscalationPolicyVersionService(private val context: DSLContext) {

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
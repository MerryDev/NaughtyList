package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyVersionRequest
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_POLICY_VERSION
import net.neruxvace.naughtylist.backend.jooq.tables.references.ESCALATION_STEP
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.types.YearToSecond
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    fun findActive(policyId: Long): EscalationPolicyVersionResponse? {
        val activeVersionId = context
            .selectFrom(ESCALATION_POLICY_VERSION)
            .where(
                ESCALATION_POLICY_VERSION.POLICY_ID.eq(policyId)
                    .and(ESCALATION_POLICY_VERSION.ACTIVE.isTrue)
            )
            .fetchOne()?.id ?: return null

        return findById(policyId, activeVersionId)
    }

    @Transactional
    fun create(policyId: Long, request: CreateEscalationPolicyVersionRequest): EscalationPolicyVersionResponse {
        request.validateForCreation()

        val policy = context
            .selectFrom(ESCALATION_POLICY)
            .where(ESCALATION_POLICY.ID.eq(policyId))
            .forUpdate()
            .fetchOne() ?: throw ResourceNotFoundException("Escalation policy not found")

        val latestVersion = context
            .select(DSL.max(ESCALATION_POLICY_VERSION.VERSION))
            .from(ESCALATION_POLICY_VERSION)
            .where(ESCALATION_POLICY_VERSION.POLICY_ID.eq(policy.id))
            .fetchOne(0, Int::class.java) ?: 0

        val versionId = context
            .insertInto(ESCALATION_POLICY_VERSION)
            .set(ESCALATION_POLICY_VERSION.POLICY_ID, policyId)
            .set(ESCALATION_POLICY_VERSION.VERSION, latestVersion + 1)
            .set(
                ESCALATION_POLICY_VERSION.LOOKBACK,
                request.lookback?.let { YearToSecond.valueOf(it) }
            )
            .returning(ESCALATION_POLICY_VERSION.ID)
            .fetchOne()?.id ?: error("Failed to create escalation policy version")

        request.steps.forEach { step ->
            context.insertInto(ESCALATION_STEP)
                .set(ESCALATION_STEP.POLICY_VERSION_ID, versionId)
                .set(ESCALATION_STEP.MINIMUM_OFFENSES, step.minimumOffenses)
                .set(ESCALATION_STEP.TYPE, step.type)
                .set(
                    ESCALATION_STEP.DURATION,
                    step.duration?.let { YearToSecond.valueOf(it) }
                )
                .execute()
        }

        return findById(policyId, versionId) ?: error("Failed to load created escalation policy version")
    }

    @Transactional
    fun activate(policyId: Long, versionId: Long): EscalationPolicyVersionResponse? {
        context.selectFrom(ESCALATION_POLICY)
            .where(ESCALATION_POLICY.ID.eq(policyId))
            .forUpdate()
            .fetchOne() ?: return null

        val version = context
            .selectFrom(ESCALATION_POLICY_VERSION)
            .where(
                ESCALATION_POLICY_VERSION.ID.eq(versionId)
                    .and(ESCALATION_POLICY_VERSION.POLICY_ID.eq(policyId))
            )
            .fetchOne() ?: return null

        if (version.active != true) {
            context.update(ESCALATION_POLICY_VERSION)
                .set(ESCALATION_POLICY_VERSION.ACTIVE, false)
                .where(
                    ESCALATION_POLICY_VERSION.POLICY_ID.eq(policyId)
                        .and(ESCALATION_POLICY_VERSION.ACTIVE.isTrue)
                )
                .execute()

            context.update(ESCALATION_POLICY_VERSION)
                .set(ESCALATION_POLICY_VERSION.ACTIVE, true)
                .where(ESCALATION_POLICY_VERSION.ID.eq(versionId))
                .execute()
        }

        return findById(policyId, versionId)
    }
}
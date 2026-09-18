package net.neruxvace.naughtylist.backend.reason

import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.jooq.tables.records.ReasonRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.reason.request.CreateReasonRequest
import net.neruxvace.naughtylist.backend.reason.request.UpdateReasonRequest
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class ReasonService(private val context: DSLContext) {

    fun findAll(): List<ReasonResponse> {
        return context
            .selectFrom(REASON)
            .orderBy(REASON.NAME.asc())
            .fetch().map(::map)
    }

    fun findById(id: Long): ReasonResponse? {
        return context
            .selectFrom(REASON)
            .where(REASON.ID.eq(id))
            .fetchOne()?.let(::map)
    }

    fun create(request: CreateReasonRequest): ReasonResponse {
        if (keyExists(request.key)) throw ResourceConflictException("A reason with key ${request.key} already exists")

        val record = context
            .insertInto(REASON)
            .set(REASON.KEY, request.key)
            .set(REASON.NAME, request.name)
            .set(REASON.DESCRIPTION, request.description)
            .returning()
            .fetchOne() ?: error("Failed to create reason with key ${request.key}")

        return map(record)
    }

    fun update(id: Long, request: UpdateReasonRequest): ReasonResponse? {
        if (!idExists(id)) return null
        if (request.isEmpty()) return findById(id)

        val update = context.updateQuery(REASON)
        request.name?.let { update.addValue(REASON.NAME, it) }
        request.description?.let { update.addValue(REASON.DESCRIPTION, it) }
        request.enabled?.let { update.addValue(REASON.ENABLED, it) }

        update.addConditions(REASON.ID.eq(id))
        update.execute()

        return findById(id)
    }

    private fun keyExists(key: String): Boolean {
        return context.fetchExists(
            context.selectOne()
                .from(REASON)
                .where(REASON.KEY.eq(key))
        )
    }

    private fun idExists(id: Long): Boolean {
        return context.fetchExists(
            context.selectOne()
                .from(REASON)
                .where(REASON.ID.eq(id))
        )
    }

    private fun map(record: ReasonRecord): ReasonResponse =
        ReasonResponse(
            id = record.id.required(),
            key = record.key,
            name = record.name,
            description = record.description,
            enabled = record.enabled.required(),
            createdAt = record.createdAt.required()
        )
}
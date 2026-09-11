package net.neruxvace.naughtylist.backend.reason

import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.reason.request.CreateReasonRequest
import net.neruxvace.naughtylist.backend.reason.request.UpdateReasonRequest
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ReasonService(private val context: DSLContext) {

    fun findAll(): List<ReasonResponse> {
        return context
            .selectFrom(REASON)
            .orderBy(REASON.NAME.asc())
            .fetch()
            .map {
                ReasonResponse(
                    id = requireNotNull(it.id),
                    key = it.key, name = it.name,
                    description = it.description,
                    enabled = requireNotNull(it.enabled),
                    createdAt = requireNotNull(it.createdAt)
                )
            }
    }

    fun findById(id: Long): ReasonResponse? {
        val record = context
            .selectFrom(REASON)
            .where(REASON.ID.eq(id))
            .fetchOne() ?: return null

        return ReasonResponse(
            id = requireNotNull(record.id),
            key = record.key, name = record.name,
            description = record.description,
            enabled = requireNotNull(record.enabled),
            createdAt = requireNotNull(record.createdAt)
        )
    }

    fun create(request: CreateReasonRequest): ReasonResponse {
        if (keyExists(request.key)) throw ResponseStatusException(HttpStatus.CONFLICT, "Reason with key ${request.key} already exists")

        val record = context
            .insertInto(REASON)
            .set(REASON.KEY, request.key)
            .set(REASON.NAME, request.name)
            .set(REASON.DESCRIPTION, request.description)
            .returning()
            .fetchOne() ?: error("Failed to create reason with key ${request.key}")

        return ReasonResponse(
            id = requireNotNull(record.id),
            key = record.key, name = record.name,
            description = record.description,
            enabled = requireNotNull(record.enabled),
            createdAt = requireNotNull(record.createdAt)
        )
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

}
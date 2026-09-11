package net.neruxvace.naughtylist.backend.reason

import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import org.jooq.DSLContext
import org.springframework.stereotype.Service

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

}
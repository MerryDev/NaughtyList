package net.neruxvace.naughtylist.backend.reason

import net.neruxvace.naughtylist.backend.exception.InvalidRequestException
import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.jooq.tables.records.ReasonRecord
import net.neruxvace.naughtylist.backend.jooq.tables.references.REASON
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.reason.request.CreateReasonRequest
import net.neruxvace.naughtylist.backend.reason.request.UpdateReasonRequest
import net.neruxvace.naughtylist.backend.web.ifPresent
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class ReasonService(private val context: DSLContext) {

    fun findAll(): List<ReasonResponse> = context
        .selectFrom(REASON)
        .orderBy(REASON.NAME.asc())
        .fetch().map(::map)

    fun findById(id: Long): ReasonResponse? = context
        .selectFrom(REASON)
        .where(REASON.ID.eq(id))
        .fetchOne()?.let(::map)

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

        request.name.ifPresent {
            update.addValue(
                REASON.NAME,
                it ?: throw InvalidRequestException("Name cannot be null")
            )
        }
        request.enabled.ifPresent {
            update.addValue(
                REASON.ENABLED,
                it ?: throw InvalidRequestException("Enabled cannot be null")
            )
        }
        request.description.ifPresent { update.addValue(REASON.DESCRIPTION, it) }

        update.addConditions(REASON.ID.eq(id))
        update.execute()

        return findById(id)
    }

    private fun keyExists(key: String): Boolean = context
        .fetchExists(
            context.selectOne()
                .from(REASON)
                .where(REASON.KEY.eq(key))
        )

    private fun idExists(id: Long): Boolean = context
        .fetchExists(
            context.selectOne()
                .from(REASON)
                .where(REASON.ID.eq(id))
        )

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
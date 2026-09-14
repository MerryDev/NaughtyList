package net.neruxvace.naughtylist.backend.auth

import net.neruxvace.naughtylist.backend.jooq.tables.references.API_CLIENT
import net.neruxvace.naughtylist.backend.persistence.required
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class ApiClientRepository(private val context: DSLContext) {

    fun findById(clientId: String): ApiClient? {
        val record = context
            .selectFrom(API_CLIENT)
            .where(API_CLIENT.CLIENT_ID.eq(clientId))
            .fetchOne() ?: return null

        return ApiClient(
            clientId = record.clientId,
            secretHash = record.secretHash,
            enabled = record.enabled.required()
        )
    }

}
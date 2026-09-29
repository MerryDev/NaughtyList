package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.jooq.tables.references.PUNISHMENT
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import kotlin.uuid.Uuid

@Service
class EscalationHistoryService(private val context: DSLContext) {

    fun countPriorPunishments(playerUuid: Uuid, reasonId: Long?, lookback: Duration?): Int {
        var condition = PUNISHMENT.PLAYER_UUID
            .eq(playerUuid)
            .and(PUNISHMENT.REVOKED_AT.isNull)

        reasonId?.let { condition = condition.and(PUNISHMENT.REASON_ID.eq(it)) }
        lookback?.let { condition = condition.and(PUNISHMENT.CREATED_AT.ge(LocalDateTime.now().minus(it))) }

        return context.fetchCount(PUNISHMENT, condition)
    }
}
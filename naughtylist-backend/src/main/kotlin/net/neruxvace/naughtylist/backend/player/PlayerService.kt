package net.neruxvace.naughtylist.backend.player

import net.neruxvace.naughtylist.backend.player.response.PlayerResponse
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import wtf.spaghetti.naughtylist.backend.jooq.tables.references.PLAYER
import wtf.spaghetti.naughtylist.backend.jooq.tables.references.PLAYER_NAME_HISTORY
import java.time.LocalDateTime
import java.util.UUID

@Service
class PlayerService(private val context: DSLContext) {

    fun findByUuid(uuid: UUID): PlayerResponse? {
        val player = context
            .selectFrom(PLAYER)
            .where(PLAYER.UUID.eq(uuid))
            .fetchOne() ?: return null

        val name = context
            .select(PLAYER_NAME_HISTORY.NAME)
            .from(PLAYER_NAME_HISTORY)
            .where(PLAYER_NAME_HISTORY.PLAYER_UUID.eq(uuid))
            .and(PLAYER_NAME_HISTORY.VALID_UNTIL.isNull)
            .fetchOne(PLAYER_NAME_HISTORY.NAME)

        return PlayerResponse(
            uuid = player.uuid,
            name = name,
            discordId = player.discordId,
            firstJoinedAt = requireNotNull(player.firstJoinedAt),
            lastSeenAt = player.lastSeenAt
        )
    }

    @Transactional
    fun sync(uuid: UUID, name: String): PlayerResponse {
        val now = LocalDateTime.now()

        context.insertInto(PLAYER)
            .set(PLAYER.UUID, uuid)
            .set(PLAYER.LAST_SEEN_AT, now)
            .onConflict(PLAYER.UUID)
            .doUpdate()
            .set(PLAYER.LAST_SEEN_AT, now)
            .execute()

        val currentName = context
            .selectFrom(PLAYER_NAME_HISTORY)
            .where(PLAYER_NAME_HISTORY.PLAYER_UUID.eq(uuid))
            .and(PLAYER_NAME_HISTORY.VALID_UNTIL.isNull)
            .fetchOne()

        if (currentName == null) { // Save name on first join
            context.insertInto(PLAYER_NAME_HISTORY)
                .set(PLAYER_NAME_HISTORY.PLAYER_UUID, uuid)
                .set(PLAYER_NAME_HISTORY.NAME, name)
                .set(PLAYER_NAME_HISTORY.VALID_FROM, now)
                .execute()

        } else if (currentName.name != name) { // Name has changed since last join
            context.update(PLAYER_NAME_HISTORY)
                .set(PLAYER_NAME_HISTORY.VALID_UNTIL, now)
                .where(PLAYER_NAME_HISTORY.ID.eq(currentName.id))
                .execute()

            context.insertInto(PLAYER_NAME_HISTORY)
                .set(PLAYER_NAME_HISTORY.PLAYER_UUID, uuid)
                .set(PLAYER_NAME_HISTORY.NAME, name)
                .set(PLAYER_NAME_HISTORY.VALID_FROM, now)
                .execute()
        }

        return requireNotNull(findByUuid(uuid))
    }
}
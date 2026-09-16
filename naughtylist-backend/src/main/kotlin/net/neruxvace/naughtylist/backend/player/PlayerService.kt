package net.neruxvace.naughtylist.backend.player

import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER
import net.neruxvace.naughtylist.backend.jooq.tables.references.PLAYER_NAME_HISTORY
import net.neruxvace.naughtylist.backend.persistence.required
import net.neruxvace.naughtylist.backend.player.response.PlayerNameResponse
import net.neruxvace.naughtylist.backend.player.response.PlayerResponse
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.uuid.Uuid

@Service
class PlayerService(private val context: DSLContext) {

    fun findByUuid(uuid: Uuid): PlayerResponse? {
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
            firstJoinedAt = player.firstJoinedAt.required(),
            lastSeenAt = player.lastSeenAt
        )
    }

    fun findByName(name: String): PlayerResponse? {
        val uuid = context
            .select(PLAYER_NAME_HISTORY.PLAYER_UUID)
            .from(PLAYER_NAME_HISTORY)
            .where(DSL.lower(PLAYER_NAME_HISTORY.NAME).eq(name.lowercase()))
            .and(PLAYER_NAME_HISTORY.VALID_UNTIL.isNull)
            .fetchOne(PLAYER_NAME_HISTORY.PLAYER_UUID) ?: return null

        return findByUuid(uuid)
    }

    fun getNameHistory(uuid: Uuid): List<PlayerNameResponse>? {
        if (!context.fetchExists(context.selectOne().from(PLAYER).where(PLAYER.UUID.eq(uuid)))) return null

        return context
            .selectFrom(PLAYER_NAME_HISTORY)
            .where(PLAYER_NAME_HISTORY.PLAYER_UUID.eq(uuid))
            .orderBy(PLAYER_NAME_HISTORY.VALID_FROM.desc())
            .fetch()
            .map {
                PlayerNameResponse(
                    name = it.name,
                    validFrom = it.validFrom.required(),
                    validUntil = it.validUntil
                )
            }
    }

    @Transactional
    fun sync(uuid: Uuid, name: String): PlayerResponse {
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
            insertName(uuid, name, now)

        } else if (currentName.name != name) { // Name has changed since last join
            context.update(PLAYER_NAME_HISTORY)
                .set(PLAYER_NAME_HISTORY.VALID_UNTIL, now)
                .where(PLAYER_NAME_HISTORY.ID.eq(currentName.id))
                .execute()

            insertName(uuid, name, now)
        }

        return requireNotNull(findByUuid(uuid))
    }

    fun updateDiscordId(uuid: Uuid, discordId: String?): PlayerResponse? {
        val updated = context
            .update(PLAYER)
            .set(PLAYER.DISCORD_ID, discordId)
            .where(PLAYER.UUID.eq(uuid))
            .execute()

        return if (updated == 0) null else findByUuid(uuid)
    }

    private fun insertName(uuid: Uuid, name: String, timestamp: LocalDateTime) {
        val inserted = context
            .insertInto(PLAYER_NAME_HISTORY)
            .set(PLAYER_NAME_HISTORY.PLAYER_UUID, uuid)
            .set(PLAYER_NAME_HISTORY.NAME, name)
            .set(PLAYER_NAME_HISTORY.VALID_FROM, timestamp)
            .onConflictDoNothing()
            .execute()

        if (inserted == 0) throw ResponseStatusException(HttpStatus.CONFLICT, "Player name is already in use")
    }
}
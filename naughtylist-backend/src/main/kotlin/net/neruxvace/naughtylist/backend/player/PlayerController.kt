package net.neruxvace.naughtylist.backend.player

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentClient
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.player.request.SyncPlayerRequest
import net.neruxvace.naughtylist.backend.player.request.UpdateDiscordIdRequest
import net.neruxvace.naughtylist.backend.player.response.PlayerNameResponse
import net.neruxvace.naughtylist.backend.player.response.PlayerResponse
import org.springframework.web.bind.annotation.*
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/players")
class PlayerController(
    private val service: PlayerService,
    private val client: CurrentClient
) {

    @PutMapping("/{uuid}")
    fun syncPlayer(@PathVariable uuid: Uuid, @Valid @RequestBody request: SyncPlayerRequest): PlayerResponse {
        client.requireServer()
        return service.sync(uuid, request.name)
    }

    @PutMapping("/{uuid}/discord")
    fun updateDiscordId(@PathVariable uuid: Uuid, @Valid @RequestBody request: UpdateDiscordIdRequest): PlayerResponse {
        return service.updateDiscordId(uuid, request.discordId) ?: throw ResourceNotFoundException("Player not found")
    }

    @GetMapping("/{uuid}")
    fun getPlayer(@PathVariable uuid: Uuid): PlayerResponse {
        return service.findByUuid(uuid) ?: throw ResourceNotFoundException("Player not found")
    }

    @GetMapping
    fun getPlayerByName(@RequestParam name: String): PlayerResponse {
        return service.findByName(name) ?: throw ResourceNotFoundException("Player not found")
    }

    @GetMapping("/{uuid}/names")
    fun getNameHistory(@PathVariable uuid: Uuid): List<PlayerNameResponse> {
        return service.getNameHistory(uuid) ?: throw ResourceNotFoundException("Player not found")
    }
}
package net.neruxvace.naughtylist.backend.player

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentClient
import net.neruxvace.naughtylist.backend.player.request.SyncPlayerRequest
import net.neruxvace.naughtylist.backend.player.response.PlayerResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/players")
class PlayerController(
    private val service: PlayerService,
    private val client: CurrentClient
) {

    @PutMapping("/{uuid}")
    fun syncPlayer(@PathVariable uuid: UUID, @Valid @RequestBody request: SyncPlayerRequest): PlayerResponse {
        client.requireServer()
        return service.sync(uuid, request.name)
    }

    @GetMapping("/{uuid}")
    fun getPlayer(@PathVariable uuid: UUID): ResponseEntity<PlayerResponse> {
        val player = service.findByUuid(uuid) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(player)
    }

    @GetMapping
    fun getPlayerByName(@RequestParam name: String): ResponseEntity<PlayerResponse> {
        val player = service.findByName(name) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(player)
    }
}
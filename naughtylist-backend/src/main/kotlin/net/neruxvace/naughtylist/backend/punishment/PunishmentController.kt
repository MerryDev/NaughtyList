package net.neruxvace.naughtylist.backend.punishment

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import net.neruxvace.naughtylist.backend.punishment.request.CreatePunishmentRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/punishments")
class PunishmentController(
    private val service: PunishmentService,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getPunishments(
        @RequestParam(required = false) playerUuid: Uuid?,
        @RequestParam(required = false) type: PunishmentType?,
        @RequestParam(required = false) reasonId: Long?,
        @RequestParam(required = false) caseId: Long?,
        @RequestParam(required = false) active: Boolean?
    ): List<PunishmentResponse> = service.findAll(playerUuid, type, reasonId, caseId, active)

    @GetMapping("/{id}")
    fun getPunishment(@PathVariable id: Long): PunishmentResponse = service.findById(id)
        ?: throw ResourceNotFoundException("Punishment not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_punishment:write')")
    fun createPunishment(@Valid @RequestBody request: CreatePunishmentRequest): PunishmentResponse {
        val actor = currentActor.requireActor()

        return service.create(request, actor.playerUuid)
    }
}
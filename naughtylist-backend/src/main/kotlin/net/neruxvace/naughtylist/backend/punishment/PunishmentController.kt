package net.neruxvace.naughtylist.backend.punishment

import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.PunishmentType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/punishments")
class PunishmentController(private val service: PunishmentService) {

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

}
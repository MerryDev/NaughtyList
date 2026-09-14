package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import net.neruxvace.naughtylist.backend.moderation.request.UpdateModerationCaseRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cases")
class ModerationCaseController(
    private val service: ModerationCaseService,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getCases(
        @RequestParam(required = false) status: CaseStatus?,
        @RequestParam(required = false) targetUuid: UUID?,
        @RequestParam(required = false) assignedTo: UUID?
    ): List<ModerationCaseResponse> {
        return service.findAll(status, targetUuid, assignedTo)
    }

    @GetMapping("/{id}")
    fun getCase(@PathVariable id: Long): ModerationCaseResponse? {
        return service.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun createCase(@RequestBody request: CreateModerationCaseRequest): ModerationCaseResponse {
        val actor = currentActor.requireActor()

        return service.create(request, actor.playerUuid)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun updateCase(@PathVariable id: Long, @RequestBody request: UpdateModerationCaseRequest): ModerationCaseResponse {
        return service.update(id, request) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun closeCase(@PathVariable id: Long) {
        service.close(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun dismissCase(@PathVariable id: Long) {
        service.dismiss(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
    }

}
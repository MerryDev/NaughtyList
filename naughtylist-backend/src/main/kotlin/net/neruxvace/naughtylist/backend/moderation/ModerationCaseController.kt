package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.jooq.enums.CaseStatus
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import net.neruxvace.naughtylist.backend.moderation.request.UpdateModerationCaseRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/cases")
class ModerationCaseController(
    private val service: ModerationCaseService,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getCases(
        @RequestParam(required = false) status: CaseStatus?,
        @RequestParam(required = false) targetUuid: Uuid?,
        @RequestParam(required = false) assignedTo: Uuid?
    ): List<ModerationCaseResponse> = service.findAll(status, targetUuid, assignedTo)

    @GetMapping("/{id}")
    fun getCase(@PathVariable id: Long): ModerationCaseResponse = service.findById(id)
        ?: throw ResourceNotFoundException("Moderation case not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun createCase(@RequestBody request: CreateModerationCaseRequest): ModerationCaseResponse {
        val actor = currentActor.requireActor()

        return service.create(request, actor.playerUuid)
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun updateCase(
        @PathVariable id: Long,
        @RequestBody request: UpdateModerationCaseRequest
    ): ModerationCaseResponse = service.update(id, request)
        ?: throw ResourceNotFoundException("Moderation case not found")

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun closeCase(@PathVariable id: Long) {
        service.close(id) ?: throw ResourceNotFoundException("Moderation case not found")
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun dismissCase(@PathVariable id: Long) {
        service.dismiss(id) ?: throw ResourceNotFoundException("Moderation case not found")
    }
}
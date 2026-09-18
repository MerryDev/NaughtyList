package net.neruxvace.naughtylist.backend.moderation.note

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.moderation.note.request.CreateCaseNoteRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cases/{caseId}/notes")
class CaseNoteController(
    private val service: CaseNoteService,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getNotes(@PathVariable caseId: Long): List<CaseNoteResponse> = service.findAllByCaseId(caseId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun createNote(
        @PathVariable caseId: Long,
        @Valid @RequestBody request: CreateCaseNoteRequest
    ): CaseNoteResponse {
        val actor = currentActor.requireActor()
        return service.create(caseId, request, actor.playerUuid)
    }

}
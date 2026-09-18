package net.neruxvace.naughtylist.backend.moderation.evidence

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.auth.CurrentActor
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/cases/{caseId}/evidence")
class CaseEvidenceController(
    private val service: CaseEvidenceService,
    private val currentActor: CurrentActor
) {

    @GetMapping
    fun getEvidence(@PathVariable caseId: Long): List<CaseEvidenceResponse> = service.findAllByCaseId(caseId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun createEvidence(
        @PathVariable caseId: Long,
        @Valid @RequestBody request: CreateCaseEvidenceRequest
    ): CaseEvidenceResponse {
        val actor = currentActor.requireActor()

        return service.create(caseId, request, actor.playerUuid)
    }

}
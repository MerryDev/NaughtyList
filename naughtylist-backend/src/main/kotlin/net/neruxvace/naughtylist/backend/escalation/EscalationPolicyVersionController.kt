package net.neruxvace.naughtylist.backend.escalation

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyVersionRequest
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/escalation-policies/{policyId}/versions")
class EscalationPolicyVersionController(
    private val service: EscalationPolicyVersionService
) {

    @GetMapping
    fun getVersions(@PathVariable policyId: Long): List<EscalationPolicyVersionResponse> = service.findAll(policyId)

    @GetMapping("/{versionId}")
    fun getVersion(
        @PathVariable policyId: Long,
        @PathVariable versionId: Long
    ): EscalationPolicyVersionResponse = service.findById(policyId, versionId)
        ?: throw ResourceNotFoundException("Escalation policy version not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_escalation:write')")
    fun createVersion(
        @PathVariable policyId: Long,
        @Valid @RequestBody request: CreateEscalationPolicyVersionRequest
    ): EscalationPolicyVersionResponse = service.create(policyId, request)

    @PostMapping("/{versionId}/activate")
    @PreAuthorize("hasAuthority('SCOPE_escalation:write')")
    fun activateVersion(
        @PathVariable policyId: Long,
        @PathVariable versionId: Long
    ): EscalationPolicyVersionResponse = service.activate(policyId, versionId)
        ?: throw ResourceNotFoundException("Escalation policy version not found")
}
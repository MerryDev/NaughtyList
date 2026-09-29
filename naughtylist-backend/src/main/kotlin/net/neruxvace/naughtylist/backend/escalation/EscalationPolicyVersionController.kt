package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/escalation-policies/{policyId}/versions")
class EscalationPolicyVersionController(
    private val service: EscalationPolicyVersionService
) {

    @GetMapping("/{versionId}")
    fun getVersion(
        @PathVariable policyId: Long,
        @PathVariable versionId: Long
    ): EscalationPolicyVersionResponse = service.findById(policyId, versionId)
        ?: throw ResourceNotFoundException("Escalation policy version not found")
}
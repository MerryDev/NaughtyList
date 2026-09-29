package net.neruxvace.naughtylist.backend.escalation

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.escalation.request.UpdateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/escalation-policies")
class EscalationPolicyController(private val service: EscalationPolicyService) {

    @GetMapping
    fun getPolicies(): List<EscalationPolicyResponse> = service.findAll()

    @GetMapping("/{id}")
    fun getPolicy(@PathVariable id: Long): EscalationPolicyResponse? = service.findById(id)
        ?: throw ResourceNotFoundException("Escalation policy with id $id not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_escalation:write')")
    fun createPolicy(
        @Valid @RequestBody request: CreateEscalationPolicyRequest
    ): EscalationPolicyResponse = service.create(request)

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_escalation:write')")
    fun updatePolicy(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateEscalationPolicyRequest
    ): EscalationPolicyResponse = service.update(id, request)
        ?: throw ResourceNotFoundException("Escalation policy with id $id not found")
}
package net.neruxvace.naughtylist.backend.escalation

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.escalation.request.CreateEscalationPolicyRequest
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
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
}
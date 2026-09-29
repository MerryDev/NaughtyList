package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/escalation-policies")
class EscalationPolicyController(private val service: EscalationPolicyService) {

    @GetMapping
    fun getPolicies(): List<EscalationPolicyResponse> = service.findAll()

    @GetMapping("/{id}")
    fun getPolicy(@PathVariable id: Long): EscalationPolicyResponse? = service.findById(id)
        ?: throw ResourceNotFoundException("Escalation policy with id $id not found")
}
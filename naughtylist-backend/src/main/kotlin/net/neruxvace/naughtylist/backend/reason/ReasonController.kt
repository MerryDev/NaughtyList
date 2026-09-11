package net.neruxvace.naughtylist.backend.reason

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.reason.request.CreateReasonRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reasons")
class ReasonController(private val service: ReasonService) {

    @GetMapping
    fun getReasons(): List<ReasonResponse> {
        return service.findAll()
    }

    @GetMapping("/{id}")
    fun getReason(@PathVariable id: Long): ResponseEntity<ReasonResponse> {
        val reason = service.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(reason)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_reason:write')")
    fun createReason(@Valid @RequestBody request: CreateReasonRequest): ReasonResponse {
        return service.create(request)
    }
}
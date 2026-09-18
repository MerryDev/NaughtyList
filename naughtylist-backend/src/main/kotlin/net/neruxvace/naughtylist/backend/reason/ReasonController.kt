package net.neruxvace.naughtylist.backend.reason

import jakarta.validation.Valid
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.reason.request.CreateReasonRequest
import net.neruxvace.naughtylist.backend.reason.request.UpdateReasonRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reasons")
class ReasonController(private val service: ReasonService) {

    @GetMapping
    fun getReasons(): List<ReasonResponse> = service.findAll()

    @GetMapping("/{id}")
    fun getReason(@PathVariable id: Long): ReasonResponse = service.findById(id)
        ?: throw ResourceNotFoundException("Reason not found")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_reason:write')")
    fun createReason(@Valid @RequestBody request: CreateReasonRequest): ReasonResponse = service.create(request)

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_reason:write')")
    fun updateReason(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateReasonRequest
    ): ReasonResponse = service.update(id, request)
        ?: throw ResourceNotFoundException("Reason not found")

}
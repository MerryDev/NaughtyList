package net.neruxvace.naughtylist.backend.moderation

import net.neruxvace.naughtylist.backend.auth.CurrentActor
import net.neruxvace.naughtylist.backend.moderation.request.CreateModerationCaseRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/cases")
class ModerationCaseController(
    private val service: ModerationCaseService,
    private val currentActor: CurrentActor
) {

    @GetMapping("/{id}")
    fun getCase(@PathVariable id: Long): ModerationCaseResponse? {
        return service.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Moderation case not found")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_case:write')")
    fun createCase(@RequestBody request: CreateModerationCaseRequest): ModerationCaseResponse {
        val actor = currentActor.requireActor()

        return service.create(request, actor.playerUuid)
    }

}
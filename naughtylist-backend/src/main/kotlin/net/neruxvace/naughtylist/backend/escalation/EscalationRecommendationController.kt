package net.neruxvace.naughtylist.backend.escalation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/v1/escalation-policies/{id}/recommendation")
class EscalationRecommendationController(
    private val service: EscalationRecommendationService
) {

    @GetMapping
    fun getRecommendation(
        @PathVariable id: Long,
        @RequestParam uuid: Uuid
    ): EscalationRecommendationResponse = service.recommend(id, uuid)

}
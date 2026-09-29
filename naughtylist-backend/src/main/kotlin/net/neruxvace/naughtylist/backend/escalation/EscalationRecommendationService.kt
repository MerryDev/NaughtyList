package net.neruxvace.naughtylist.backend.escalation

import net.neruxvace.naughtylist.backend.exception.ResourceConflictException
import net.neruxvace.naughtylist.backend.exception.ResourceNotFoundException
import net.neruxvace.naughtylist.backend.player.PlayerService
import org.springframework.stereotype.Service
import kotlin.uuid.Uuid

@Service
class EscalationRecommendationService(
    private val policies: EscalationPolicyService,
    private val versions: EscalationPolicyVersionService,
    private val history: EscalationHistoryService,
    private val players: PlayerService
) {

    fun recommend(policyId: Long, playerUuid: Uuid): EscalationRecommendationResponse {
        val policy = policies.findById(policyId) ?: throw ResourceNotFoundException("Escalation policy not found")

        if (!policy.enabled) {
            throw ResourceConflictException("Escalation policy is disabled")
        }
        if (players.findByUuid(playerUuid) == null) {
            throw ResourceNotFoundException("Player not found")
        }

        val version = versions.findActive(policyId) ?: throw ResourceConflictException("Escalation policy has no active version")
        val offenseNumber = history.countPriorPunishments(playerUuid, policy.reasonId, version.lookback) + 1

        return EscalationRecommendationResponse(
            id = policyId,
            policyVersionId = version.id,
            offenseNumber = offenseNumber,
            step = version.stepFor(offenseNumber)
        )
    }
}
package net.neruxvace.naughtylist.backend.punishment.request

import jakarta.validation.constraints.NotBlank

data class RevokePunishmentRequest(@NotBlank val reason: String)
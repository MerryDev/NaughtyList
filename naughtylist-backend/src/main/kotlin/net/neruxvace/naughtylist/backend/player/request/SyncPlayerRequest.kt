package net.neruxvace.naughtylist.backend.player.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SyncPlayerRequest(@NotBlank @Pattern(regexp = "^[a-zA-Z0-9_]{3,16}$") val name: String)

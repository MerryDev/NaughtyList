package net.neruxvace.naughtylist.backend.reason.request

import jakarta.validation.constraints.NotBlank

data class CreateReasonRequest(
    @NotBlank val key: String,
    @NotBlank val name: String,
    val description: String? = null
)

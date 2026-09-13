package net.neruxvace.naughtylist.backend.moderation.note.request

import jakarta.validation.constraints.NotBlank

data class CreateCaseNoteRequest(@NotBlank val content: String)

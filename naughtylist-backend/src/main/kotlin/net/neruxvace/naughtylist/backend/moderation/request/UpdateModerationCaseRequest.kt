package net.neruxvace.naughtylist.backend.moderation.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import net.neruxvace.naughtylist.backend.web.Patchable
import net.neruxvace.naughtylist.backend.web.Patchable.Absent
import net.neruxvace.naughtylist.backend.web.Patchable.Present
import kotlin.uuid.Uuid

class UpdateModerationCaseRequest {

    @get:JsonIgnore
    var title: Patchable<String> = Absent
        private set

    @get:JsonIgnore
    var summary: Patchable<String> = Absent
        private set

    @get:JsonIgnore
    var assignedTo: Patchable<Uuid> = Absent
        private set

    @JsonSetter("title")
    fun readTitle(value: String?) {
        title = Present(value)
    }

    @JsonSetter("summary")
    fun readSummary(value: String?) {
        summary = Present(value)
    }

    @JsonSetter("assignedTo")
    fun readAssignedTo(value: Uuid?) {
        assignedTo = Present(value)
    }

    fun isEmpty(): Boolean = title is Absent && summary is Absent && assignedTo is Absent

}

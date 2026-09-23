package net.neruxvace.naughtylist.backend.reason.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import net.neruxvace.naughtylist.backend.web.Patchable
import net.neruxvace.naughtylist.backend.web.Patchable.Absent
import net.neruxvace.naughtylist.backend.web.Patchable.Present

class UpdateReasonRequest {

    @get:JsonIgnore
    var name: Patchable<String> = Absent
        private set

    @get:JsonIgnore
    var description: Patchable<String> = Absent
        private set

    @get:JsonIgnore
    var enabled: Patchable<Boolean> = Absent
        private set

    @JsonSetter("name")
    fun readName(value: String?) {
        name = Present(value)
    }

    @JsonSetter("description")
    fun readDescription(value: String?) {
        description = Present(value)
    }

    @JsonSetter("enabled")
    fun readEnabled(value: Boolean?) {
        enabled = Present(value)
    }

    fun isEmpty(): Boolean = name is Absent && description is Absent && enabled is Absent
}
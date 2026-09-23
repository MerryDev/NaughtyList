package net.neruxvace.naughtylist.backend.web

import net.neruxvace.naughtylist.backend.moderation.request.UpdateModerationCaseRequest
import net.neruxvace.naughtylist.backend.reason.request.UpdateReasonRequest
import net.neruxvace.naughtylist.backend.web.Patchable.Absent
import net.neruxvace.naughtylist.backend.web.Patchable.Present
import tools.jackson.databind.json.JsonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatchableRequestTest {

    private val mapper = JsonMapper.builder().build()

    @Test
    fun `distinguishes omitted case fields from explicit null`() {
        val request = mapper.readValue(
            """{"summary":null}""",
            UpdateModerationCaseRequest::class.java
        )

        assertEquals(Absent, request.title)
        assertEquals(Present<String>(null), request.summary)
        assertEquals(Absent, request.assignedTo)
        assertFalse(request.isEmpty())
    }

    @Test
    fun `distinguishes omitted reason fields from explicit null`() {
        val request = mapper.readValue(
            """{"description":null}""",
            UpdateReasonRequest::class.java
        )

        assertEquals(Absent, request.name)
        assertEquals(Present<String>(null), request.description)
        assertEquals(Absent, request.enabled)
        assertFalse(request.isEmpty())
    }

    @Test
    fun `recognizes empty patch requests`() {
        val caseRequest = mapper.readValue(
            "{}",
            UpdateModerationCaseRequest::class.java
        )
        val reasonRequest = mapper.readValue(
            "{}",
            UpdateReasonRequest::class.java
        )

        assertTrue(caseRequest.isEmpty())
        assertTrue(reasonRequest.isEmpty())
    }
}
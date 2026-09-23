package net.neruxvace.naughtylist.backend.persistence

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinUuidConverterTest {

    private val testUuid = "550e8400-e29b-41d4-a716-446655440000"
    private val converter = KotlinUuidConverter()

    @Test
    fun `converts UUID in both directions`() {
        val databaseUuid = UUID.fromString(testUuid)
        val kotlinUuid = converter.from(databaseUuid)

        assertEquals(testUuid, kotlinUuid.toString())
        assertEquals(databaseUuid, converter.to(kotlinUuid))
    }

}
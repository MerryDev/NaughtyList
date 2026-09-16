package net.neruxvace.naughtylist.backend.config

import org.springframework.format.support.DefaultFormattingConversionService
import tools.jackson.databind.json.JsonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class UuidConfigurationTest {

    private val testUuid = "550e8400-e29b-41d4-a716-446655440000"
    private val configuration = UuidConfiguration()

    @Test
    fun `converts request value to Kotlin UUID`() {
        val conversionService = DefaultFormattingConversionService()
        configuration.addFormatters(conversionService)

        assertEquals(
            Uuid.parse(testUuid),
            conversionService.convert(testUuid, Uuid::class.java)
        )
    }

    @Test
    fun `maps UUID to and from JSON string`() {
        val mapper = JsonMapper.builder()
            .addModule(configuration.uuidJacksonModule())
            .build()

        val uuid = Uuid.parse(testUuid)
        val json = mapper.writeValueAsString(uuid)

        assertEquals("\"${testUuid}\"", json)
        assertEquals(uuid, mapper.readValue(json, Uuid::class.java))
    }
}
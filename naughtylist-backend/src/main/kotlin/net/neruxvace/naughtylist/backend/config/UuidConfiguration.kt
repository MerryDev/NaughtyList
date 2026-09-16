package net.neruxvace.naughtylist.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.deser.std.FromStringDeserializer
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdScalarSerializer
import kotlin.uuid.Uuid

@Configuration
class UuidConfiguration : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(String::class.java, Uuid::class.java) { value -> Uuid.parse(value) }
    }

    @Bean
    fun uuidJacksonModule(): SimpleModule = SimpleModule("kotlin-uuid").apply {
        addSerializer(Uuid::class.java, UuidSerializer)
        addDeserializer(Uuid::class.java, UuidDeserializer)
    }

    private object UuidSerializer : StdScalarSerializer<Uuid>(Uuid::class.java) {
        override fun serialize(value: Uuid, generator: JsonGenerator, context: SerializationContext) {
            generator.writeString(value.toString())
        }
    }

    private object UuidDeserializer : FromStringDeserializer<Uuid>(Uuid::class.java) {
        override fun _deserialize(value: String, context: DeserializationContext): Uuid = Uuid.parse(value)
    }
}
package net.neruxvace.naughtylist.backend.persistence

import org.jooq.impl.AbstractConverter
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class KotlinUuidConverter : AbstractConverter<UUID, Uuid>(UUID::class.java, Uuid::class.java) {

    override fun from(databaseObject: UUID?): Uuid? = databaseObject?.toKotlinUuid()

    override fun to(userObject: Uuid?): UUID? = userObject?.toJavaUuid()
}
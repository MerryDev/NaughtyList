package net.neruxvace.naughtylist.backend.auth

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import net.neruxvace.naughtylist.backend.jooq.tables.references.API_CLIENT

@Component
class ApiClientBootstrap(
    private val context: DSLContext,
    private val encoder: PasswordEncoder,
    private val environment: Environment
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(ApiClientBootstrap::class.java)

    override fun run(vararg args: String) {
        val clientId = environment.getProperty("BOOTSTRAP_CLIENT_ID") ?: return
        val clientName = environment.getProperty("BOOTSTRAP_CLIENT_NAME") ?: return
        val clientSecret = environment.getProperty("BOOTSTRAP_CLIENT_SECRET") ?: return

        val exists = context.fetchExists(context.selectOne().from(API_CLIENT).where(API_CLIENT.CLIENT_ID.eq(clientId)))
        if (exists) return

        context.insertInto(API_CLIENT)
            .set(API_CLIENT.CLIENT_ID, clientId)
            .set(API_CLIENT.NAME, clientName)
            .set(API_CLIENT.SECRET_HASH, encoder.encode(clientSecret))
            .set(API_CLIENT.ENABLED, true)
            .execute()

        logger.info("Bootstrap API user '{}' was successfully created.", clientId)
    }

}

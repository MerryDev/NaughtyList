import org.gradle.api.Project
import java.util.*

fun Project.mavenArtifact(): String {
    val rawName = property("plugin-name") as String
    return rawName.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9\\-.]"), "-")
        .replace(Regex("-+"), "-")
        .trim('-')
}

fun Project.minecraftPluginMainClass(): String {
    val rawName = property("plugin-name") as String
    val name = rawName
        .split(Regex("[\\s_-]+"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
        .replace(Regex("[^A-Za-z0-9]"), "")
        .replace(Regex("^[0-9]+"), "")

    return "${name}Plugin"
}

fun Project.pluginAuthors(defaultAuthors: List<String> = listOf("Unknown")): List<String> {
    val rawList = property("authors") as? String? ?: return defaultAuthors

    return rawList
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.isNotBlank() }
}

fun Project.configurePaperServer(runDirName: String, port: String) {
    val serverDir = layout.dir(provider { file(runDirName) }).get().asFile
    serverDir.mkdirs()

    // Auto accept the eula
    val eulaFile = serverDir.resolve("eula.txt")
    eulaFile.writeText("eula=true")

    // Set port of the paper backend server and disable online mode
    val propertiesFile = serverDir.resolve("server.properties")
    val properties = Properties()
    if (propertiesFile.exists()) {
        properties.load(propertiesFile.inputStream())
    }

    properties.setProperty("server-port", port)
    properties.setProperty("online-mode", "false")
    properties.store(propertiesFile.outputStream(), null)

    // Enable velocity
    val configDir = serverDir.resolve("config")
    configDir.mkdirs()

    val secret = DevEnvironment.readForwardingSecret(rootDir)
    configDir.resolve("paper-global.yml").writeText(
        """
             proxies:
              velocity:
                enabled: true
                online-mode: true
                secret: "$secret"
        """.trimIndent(), Charsets.UTF_8
    )
}

fun Project.configureVelocityProxy(servers: Map<String, String>) {
    val secretFile = DevEnvironment.ensureForwardingSecretFile(rootDir)

    val serverDir = layout.dir(provider { file("run") }).get().asFile
    serverDir.mkdirs()

    val toml = serverDir.resolve("velocity.toml")
    if (toml.exists()) toml.delete()

    val serverEntries = servers.entries.joinToString("\n") { "${it.key} = \"127.0.0.1:${it.value}\"" }

    toml.writeText(
        """
            config-version = "2.7"
            bind = "0.0.0.0:25565"
            online-mode = true
            player-info-forwarding-mode = "modern"
            forwarding-secret-file = "${secretFile.absolutePath.replace("\\", "/")}"
            
            [servers]
            $serverEntries

            try = [
              "lobby"
            ]
            
            [forced-hosts]
            lobby = "lobby"
        """.trimIndent(), Charsets.UTF_8
    )
}
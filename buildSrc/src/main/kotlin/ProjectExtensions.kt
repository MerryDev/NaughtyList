import org.gradle.api.Project

fun Project.mavenArtifact(): String {
    val rawName = property("plugin-name") as String
    return rawName.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9\\-\\.]"), "-")
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
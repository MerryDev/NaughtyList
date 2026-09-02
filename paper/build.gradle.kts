plugins {
    alias(libs.plugins.bukkitConvention)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":common"))
}

tasks {
    val artifact = project.mavenArtifact()

    shadowJar {
        from(sourceSets.main.get().output)

        // Dieses Mapping sorgt dafür, dass die Klassen des Dependencies-Pakets
        // in einen eigenen Namespace verschoben werden, wenn der Shadow-JAR gebaut wird.
        // So vermeiden wir Konflikte mit anderen Libraries, die dieselben Klassen enthalten.
        // Format: originalPackage → relocatedPackage
        // Beispiel: io.github.foo → foo

        // Entferne die nachfolgende Kommentierung, sobald eine Library in das Plugin fest zur Laufzeit integriert werden muss.

        /*
        val mapping = mapOf("" to "")

        val base = "$group.$artifact.paper.libs"
        for ((pattern, name) in mapping) relocate(pattern, "$base.$name")
         */

        archiveFileName = "$artifact-${rootProject.version}-paper.jar"
    }

    bukkitPluginYaml {
        val mainClass = project.minecraftPluginMainClass()

        main = "$group.$artifact.paper.$mainClass"
        name = rootProject.property("plugin-name") as String
        authors = project.pluginAuthors()
    }

    runServer {
        minecraftVersion("1.21.11")
    }
}
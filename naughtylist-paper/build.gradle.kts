import xyz.jpenilla.runpaper.task.RunServer

plugins {
    alias(libs.plugins.bukkitConvention)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":naughtylist-common"))
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

    registerBackendServer("runLobby", "run-lobby", DevEnvironment.LOBBY_PORT)
    registerBackendServer("runGame", "run-game", DevEnvironment.GAME_PORT)
}

fun registerBackendServer(name: String, runDirName: String, port: String) {
    val copyTask = tasks.register<CopyPlugin>("copy${name}Plugin") {
        runDir.set(runDirName)

        dependsOn("shadowJar")
        from(tasks.named("shadowJar"))
        into(layout.dir(provider { file("${runDir.get()}/plugins") }))
        doFirst {
            println("Copying plugin into ${runDir.get()}/plugins")
        }
    }

    tasks.register<RunServer>(name) {
        minecraftVersion("26.2")
        runDirectory = file(runDirName)
        dependsOn(copyTask)
        doFirst {
            configurePaperServer(runDirName, port)
        }
        downloadPlugins {
            url("https://download.luckperms.net/1631/bukkit/loader/LuckPerms-Bukkit-5.5.42.jar")
        }
    }
}
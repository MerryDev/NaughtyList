import xyz.jpenilla.runvelocity.task.RunVelocity

plugins {
    alias(libs.plugins.velocityConvention)
    alias(libs.plugins.runVelocity)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)

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

        val base = "$group.$artifact.velocity.libs"
        for ((pattern, name) in mapping) relocate(pattern, "$base.$name")
         */

        archiveFileName = "$artifact-${rootProject.version}-velocity.jar"
    }

    velocityPluginJson {
        val mainClass = project.minecraftPluginMainClass()

        main = "$group.$artifact.velocity.$mainClass"
        name = rootProject.property("plugin-name") as String
        authors = project.pluginAuthors()
    }

    register<RunVelocity>("runProxy") {
        dependsOn("copyVelocityPlugin")

        velocityVersion(libs.versions.velocity.get())
        doFirst {
            configureVelocityProxy(
                mapOf(
                    "lobby" to DevEnvironment.LOBBY_PORT,
                    "game" to DevEnvironment.GAME_PORT
                )
            )
        }
        downloadPlugins {
            url("https://download.luckperms.net/1668/velocity/LuckPerms-Velocity-5.5.81.jar")
        }
    }

    register<Copy>("copyVelocityPlugin") {
        dependsOn(shadowJar)

        println("Copying plugin into data directory")
        from(shadowJar)
        into(layout.dir(provider { file("run/plugins") }))
    }
}

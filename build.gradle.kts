plugins {
    java
}

allprojects {
    group = "net.crystalixs"
    version = project.property("version") as String

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply<JavaPlugin>()

    tasks {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }

        compileJava {
            options.encoding = "UTF-8"
            options.release.set(25)
        }

        compileTestJava {
            options.encoding = "UTF-8"
            options.release.set(25)
        }
    }
}
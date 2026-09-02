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
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        compileJava {
            options.encoding = "UTF-8"
            options.release.set(21)
        }

        compileTestJava {
            options.encoding = "UTF-8"
            options.release.set(21)
        }
    }
}
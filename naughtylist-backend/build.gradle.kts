import io.github.klahap.dotenv.DotEnvBuilder

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.jooq.codegen)
    alias(libs.plugins.dotenv)
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
}

dependencies {
    implementation(libs.spring.flyway)
    implementation(libs.spring.jooq)
    implementation(libs.spring.validation)
    implementation(libs.spring.webmvc)
    implementation(libs.flyway.postgresql)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.kotlin)

    runtimeOnly(libs.postgresql)
    jooqCodegen(libs.postgresql)

    testImplementation(libs.spring.flyway.test)
    testImplementation(libs.spring.jooq.test)
    testImplementation(libs.spring.validation.test)
    testImplementation(libs.spring.webmvc.test)
    testImplementation(libs.kotlin.junit5.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    jooq {
        val artifact = project.mavenArtifact()
        val envVars = DotEnvBuilder.dotEnv { addFile(project.file("$rootDir/secrets/database-credentials.env")) }

        configuration {
            jdbc {
                driver = "org.postgresql.Driver"
                url = envVars["DB_URL"]
                user = envVars["DB_USERNAME"]
                password = envVars["DB_PASSWORD"]
            }
            generator {
                name = "org.jooq.codegen.KotlinGenerator"
                database {
                    name = "org.jooq.meta.postgres.PostgresDatabase"
                    inputSchema = "public"
                }
                target {
                    packageName = "$group.$artifact.backend.jooq"
                    directory = "build/generated-src/jooq/main"
                }
            }
        }
    }

    compileKotlin {
        dependsOn(jooqCodegen)
    }
}

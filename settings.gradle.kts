pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.spring.io/snapshot")
    }
}

rootProject.name = "NaughtyList"

include("naughtylist-backend")
include("naughtylist-common")
include("naughtylist-paper")
include("naughtylist-velocity")
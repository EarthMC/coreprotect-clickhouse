pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.earthmc.net/public")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "coreprotect-clickhouse"

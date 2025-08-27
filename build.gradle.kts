plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.0.0-rc1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gorylenko.gradle-git-properties") version "2.5.2"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j")
    }

    implementation(libs.bstats)
    implementation(libs.clickhouse.jdbc)

    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.45"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") {
        exclude(group = "*", module = "FastAsyncWorldEdit-Core")
    }
    compileOnly("com.github.DeadSilenceIV:AdvancedChestsAPI:3.2-BETA")

    testImplementation(platform("org.junit:junit-bom:5.13.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21

    withJavadocJar()
    withSourcesJar()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
    }

    shadowJar {
        archiveClassifier.set(libs.versions.minecraft)

        // automatically disable relocations when running via debugger
        val disableRelocation = project.hasProperty("idea.debugger.dispatch.addr") || project.hasProperty("disableRelocation")

        if (!disableRelocation) {
            relocate("org.bstats", "net.coreprotect.libs.bstats")
            relocate("com.zaxxer.hikari", "net.coreprotect.libs.hikaricp")
            relocate("com.clickhouse", "net.coreprotect.libs.clickhouse")
        }

        dependencies {
            exclude(dependency("com.google.code.gson:.*"))
            exclude(dependency("org.intellij:.*"))
            exclude(dependency("org.jetbrains:.*"))
            exclude(dependency("net.java.dev.jna:.*"))
            exclude(dependency("org.jspecify:.*"))
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        expand(
            "version" to project.version,
            "branch" to "development",
            "api_version" to libs.versions.minecraft.get()
        )
    }

    test {
        useJUnitPlatform()
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.id.abbrev", "git.remote.origin.url", "git.commit.message.short")
}

publishing {
    repositories {
        maven {
            val releasesUrl = "https://repo.earthmc.net/releases"
            val snapshotsUrl = "https://repo.earthmc.net/snapshots"
            url = uri(if (project.version.toString().endsWith("-SNAPSHOT")) snapshotsUrl else releasesUrl)

            name = "earthmc"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
        }
    }
}

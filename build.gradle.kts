plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle(libs.paper.get().version)
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j")
    }

    implementation(platform("com.intellectualsites.bom:bom-newest:1.45"))
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.clickhouse:clickhouse-jdbc:0.8.5")

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

java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.4")
    }

    shadowJar {
        archiveClassifier.set("")

        relocate("org.bstats", "net.coreprotect.libs.bstats")
        relocate("com.zaxxer.hikari", "net.coreprotect.libs.hikaricp")
        relocate("com.clickhouse", "net.coreprotect.libs.clickhouse")

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
            "branch" to "development"
        )
    }

    test {
        useJUnitPlatform()
    }
}

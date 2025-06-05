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
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.oshi:oshi-core:6.6.2")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("com.clickhouse:clickhouse-jdbc:0.8.6")

    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") {
        exclude(group = "*", module = "FastAsyncWorldEdit-Core")
    }
    compileOnly("com.github.DeadSilenceIV:AdvancedChestsAPI:3.2-BETA")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testImplementation("org.mockito:mockito-core:5.16.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.16.1")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.133.2")
    testImplementation("org.xerial:sqlite-jdbc:3.45.1.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.12")
    testImplementation("net.bytebuddy:byte-buddy:1.16.1")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.16.1")
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

        relocate("org.bstats", "net.coreprotect")
        relocate("com.zaxxer", "net.coreprotect")

        dependencies {
            exclude(dependency("com.google.code.gson:.*"))
            exclude(dependency("org.intellij:.*"))
            exclude(dependency("org.jetbrains:.*"))
            exclude(dependency("org.slf4j:.*"))
            exclude(dependency("org.apache.logging.log4j:.*"))
            exclude(dependency("net.java.dev.jna:.*"))
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "9.1.0"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // wasm にするには jvmMain/ にこのあたりは書かないといけない
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")

    testImplementation(kotlin("reflect"))
    testImplementation("io.github.takenoko4096:mojangson-bridge:0.2.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveFileName.set("mcflow-lsp.jar")
        manifest {
            attributes["Main-Class"] = "io.github.takenoko4096.bcl.server.MainKt"
        }
    }

    register<Copy>("putJar") {
        val shadowJarTask = named<ShadowJar>("shadowJar")

        dependsOn(shadowJarTask)

        from(shadowJarTask.flatMap { it.archiveFile })

        into("extension/server")
    }

    named("shadowJar") {
        finalizedBy("putJar")
    }
}

application {
    mainClass.set("io.github.takenoko4096.mcflow.lsp.MainKt")
}

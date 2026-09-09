plugins {
    kotlin("jvm") version "2.3.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
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
}

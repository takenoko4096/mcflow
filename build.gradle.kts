plugins {
    kotlin("jvm") version "2.3.10"
}

group = "io.github.takenoko4096"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")
}

kotlin {
    jvmToolchain(25)
}

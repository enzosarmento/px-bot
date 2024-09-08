plugins {
    kotlin("jvm") version "2.0.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.kord:kord-core:0.14.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("dev.schlaubi.lavakord:kord-jvm:7.1.0")
    implementation("dev.schlaubi.lavakord:lavasrc-jvm:7.1.0") {
        exclude(group = "com.github.topi314.lavasrc", module = "protocol")
    }
    implementation("dev.schlaubi.lavakord:sponsorblock-jvm:7.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    kotlin("jvm") version "2.3.10"
}

group = "ch.flavianz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.7.3")

    // MongoDB Kotlin/sync driver
    implementation("org.mongodb:mongodb-driver-sync:5.1.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.17")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Dnet.bytebuddy.experimental=true")
}
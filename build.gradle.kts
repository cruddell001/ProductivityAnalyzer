plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.ruddell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "2.3.10"

    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.2")
    implementation("io.ktor:ktor-client-cio-jvm:2.2.2")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("org.json:json:20210307")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}
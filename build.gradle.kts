import java.util.*

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.ruddell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "2.3.10"
    val logback_version = "1.4.14"

    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.2")
    implementation("io.ktor:ktor-client-cio-jvm:2.2.2")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("org.json:json:20210307")
    implementation("ch.qos.logback:logback-classic:$logback_version")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

fun buildConfig(props: Properties) {
    println("buildConfig()")
    val outDir = File("src/main/generated")
    outDir.mkdirs()
    val file = File(outDir, "BuildConfig.kt")

    val entries = props.keys.map {
        "\tval ${it.toString().replace(".", "_")} = \"${props[it]}\""
    }
    val contents = """
object BuildConfig {
${entries.joinToString("\n")}
}
    """.trimIndent()
    file.writeText(contents)
}

// create task called Setup that runs before assemble:
val setup by tasks.registering {
    doLast {
        Properties().apply {
            load(File("local.properties").inputStream())
            buildConfig(this)
        }
    }
}

application {
    Properties().apply {
        load(File("local.properties").inputStream())
        buildConfig(this)
    }
    mainClass.set("com.ruddell.MainKt")
    sourceSets {
        val generatedDir = File("src/main/generated")
        val main = getByName("main")
        main.java.srcDir(generatedDir)
    }
}

tasks.named("assemble") {
    dependsOn(setup)
}

tasks.named("build") {
    dependsOn(setup)
}

tasks.named("run") {
    dependsOn(setup)
}

tasks.named("shadowJar") {
    dependsOn(setup)
}
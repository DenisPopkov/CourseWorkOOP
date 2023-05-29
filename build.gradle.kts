val ktor_version: String by project
val coroutines_version: String by project
val serialization_version: String by project
val kotlinx_html_version: String by project
val kotlinx_datetime_version: String by project

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.10"
    id("org.jetbrains.compose") version "1.4.0-alpha01-dev977"
    id("io.ktor.plugin") version "2.2.4"
    application
}

val ktorVersion = "2.2.2"
val kotestVersion = "5.5.4"

group = "ru.popkov.coursework"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        withJava()
    }
    js(IR) {
        browser {
            testTask {
                testLogging.showStandardStreams = true
                useKarma {
                    useSafari()
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation("org.mongodb:bson:4.9.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-resources:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
                implementation("io.ktor:ktor-serialization-kotlinx-protobuf:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val exposedVersion = "0.41.1"
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.mongodb:mongodb-driver-sync:4.2.3")
                implementation("io.ktor:ktor-server-html-builder-jvm")
                implementation("io.ktor:ktor-server-cors")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-server-websockets")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")
                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")
                implementation("org.litote.kmongo:kmongo-serialization:4.9.0")
                implementation("org.litote.kmongo:kmongo-id-serialization:4.9.0")
                implementation("org.mongodb:bson:4.9.1")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                implementation("org.litote.kmongo:kmongo-serialization:4.9.0")
                implementation("org.litote.kmongo:kmongo-id-serialization:4.9.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutines_version")
                implementation("io.ktor:ktor-client-js:$ktor_version")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
dependencies {
    testImplementation(project(mapOf("path" to ":")))
    testImplementation(project(mapOf("path" to ":")))
    testImplementation(project(mapOf("path" to ":")))
    testImplementation(project(mapOf("path" to ":")))
}

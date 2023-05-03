val ktor_version: String by project
val coroutines_version: String by project
val serialization_version: String by project
val kotlinx_html_version: String by project
val kotlinx_datetime_version: String by project

plugins {
    kotlin("multiplatform") version "1.8.10"
    id("org.jetbrains.compose") version "1.4.0-alpha01-dev977"
    id("io.ktor.plugin") version "2.2.4"
    kotlin("plugin.serialization") version "1.8.10"
    application
}

group = "ru.popkov.transport.timer.server"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

application {
    mainClass.set("ru.popkov.transport.timer.server.application.MainKt")
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
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-html-builder-jvm")
                implementation("io.ktor:ktor-server-cors")
                implementation("io.ktor:ktor-server-websockets")
                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")
                implementation("com.google.firebase:firebase-bom:31.2.3")
                implementation("com.google.firebase:firebase-firestore-ktx:24.4.5")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutines_version")
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation("com.google.firebase:firebase-bom:31.2.3")
                implementation("dev.gitlive:firebase-firestore-js:1.7.2")
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
    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")
    }
}
dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

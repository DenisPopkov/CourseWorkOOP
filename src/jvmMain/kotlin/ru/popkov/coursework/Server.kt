package ru.popkov.coursework

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import ru.popkov.coursework.repo.createTestData
import ru.popkov.coursework.rest.gradeRoutes
import ru.popkov.coursework.rest.lessonRoutes
import ru.popkov.coursework.rest.studentRoutes

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "127.0.0.1",
        watchPaths = listOf("classes")
    ) {
        main()
    }.start(wait = true)
}

fun Application.main(isTest: Boolean = true) {
    config(isTest)
    rest()
    if (isTest) logRoute()
}

fun Application.config(isTest: Boolean) {
    install(ContentNegotiation) {
        json()
    }
    if (isTest) {
        createTestData()
        install(createApplicationPlugin("DelayEmulator") {
            onCall {
                delay(1000L)
            }
        })
    }
}

fun Application.rest() {
    routing {
        studentRoutes()
        lessonRoutes()
        gradeRoutes()
    }
}
package ru.popkov.coursework.rest

import config.Config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.popkov.coursework.repo.gradesRepo

fun Route.gradeRoutes() {
    route(Config.gradesPath) {
        repoRoutes(gradesRepo)
        get("ByStartName/{startName}") {
            val startName =
                call.parameters["startName"] ?: return@get call.respondText(
                    "Missing or malformed startName",
                    status = HttpStatusCode.BadRequest
                )
            val grades = gradesRepo.read().filter {
                it.elem.grade?.mark == 5
            }
            if (grades.isEmpty()) {
                call.respondText("No grades found", status = HttpStatusCode.NotFound)
            } else {
                call.respond(grades)
            }
        }
    }
}
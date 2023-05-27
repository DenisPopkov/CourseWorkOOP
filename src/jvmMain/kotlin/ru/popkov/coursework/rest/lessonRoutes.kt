package ru.popkov.coursework.rest

import config.Config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.popkov.coursework.repo.lessonsRepo
import ru.popkov.coursework.repo.studentsRepo

fun Route.lessonRoutes() {
    route(Config.lessonsPath) {
        repoRoutes(lessonsRepo)

        get("{idL}/students/{idS}") {
            val idL = call.parameters["idL"]
                ?: return@get call.respondText(
                    "Missing or malformed lesson id",
                    status = HttpStatusCode.BadRequest
                )
            val lesson = lessonsRepo.read(listOf(idL)).getOrNull(0)
                ?: return@get call.respondText(
                    "No lesson with id $idL",
                    status = HttpStatusCode.NotFound
                )
            val idS = call.parameters["idS"] ?: return@get call.respondText(
                "Missing or malformed student id",
                status = HttpStatusCode.BadRequest
            )
            studentsRepo.read(listOf(idS)).getOrNull(0)
                ?: return@get call.respondText(
                    "No student with id $idS",
                    status = HttpStatusCode.NotFound
                )
            if (lesson.elem.students.find { it.studentId == idS } != null)
                return@get call.respondText(
                    "Student already in lesson",
                    status = HttpStatusCode.BadRequest
                )
            val newLesson = lesson.elem.addStudent(idS)
            lessonsRepo.update(lesson.id, newLesson)

            call.respondText(
                "Student added correctly",
                status = HttpStatusCode.OK
            )
        }
    }
}

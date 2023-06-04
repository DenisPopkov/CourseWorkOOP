import common.Item
import data.Grade
import data.GradeInfo
import data.Lesson
import data.Student
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.litote.kmongo.json
import ru.popkov.coursework.main

class ApplicationTest : StringSpec({
    "Students routes" {
        testApplication {
            application {
                main()
            }
            val students = withClue("read") {
                val response = client.get("/students/")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<List<Item<Student>>>(response.bodyAsText()).apply {
                    size shouldBe 4
                }
            }
            val lessons = withClue("read lessons") {
                val response = client.get("/lessons/")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<List<Item<Lesson>>>(response.bodyAsText()).apply {
                    size shouldBe 3
                }
            }
            withClue("read lessons id") {
                val sheldon = lessons.first { it.elem.name == "Math" }
                val response = client.get("/students/${sheldon.id}")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<Item<Student>>(response.body()).apply {
                    elem.firstname shouldBe "Sheldon"
                }
            }
            val grades = withClue("read grades") {
                val response = client.get("/grades/")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<List<Item<GradeInfo>>>(response.bodyAsText()).apply {
                    size shouldBe 1
                }
            }
            withClue("update grade") {
                client.put("/grades/${grades.first().id}") {
                    contentType(ContentType.Application.Json)
                    setBody(GradeInfo(grades.first().id, Grade.B).json)
                }
                Json.decodeFromString<Item<GradeInfo>>(
                    client.get("/grades/${grades.first().id}").bodyAsText()
                ).apply {
                    elem.grade?.mark shouldBe 4
                }
            }
            withClue("read id") {
                val sheldon = students.first { it.elem.firstname == "Sheldon" }
                val response = client.get("/students/${sheldon.id}")
                response.status shouldBe HttpStatusCode.OK
                Json.decodeFromString<Item<Student>>(response.body()).apply {
                    elem.firstname shouldBe "Sheldon"
                }
            }
            val newStudents = withClue("create") {
                val response = client.post("/students/") {
                    contentType(ContentType.Application.Json)
                    setBody(Student("Raj", "Koothrappali").json)
                }
                response.status shouldBe HttpStatusCode.Created
                Json.decodeFromString<List<Item<Student>>>(
                    client.get("/students/").bodyAsText()
                ).apply {
                    size shouldBe 5
                }
            }
            val emi = withClue("update") {
                val raj = newStudents.first { it.elem.firstname == "Raj" }
                client.put("/students/${raj.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(Student("Sheldon", "Fowler").json)
                }
                Json.decodeFromString<Item<Student>>(
                    client.get("/students/${raj.id}").bodyAsText()
                ).apply {
                    elem.firstname shouldBe "Sheldon"
                }
            }
            withClue("delete") {
                client.delete("/students/${emi.id}")
                Json.decodeFromString<List<Item<Student>>>(
                    client.get("/students/").bodyAsText()
                ).apply {
                    size shouldBe 4
                }
            }
            withClue("delete lesson") {
                client.delete("/lessons/${emi.id}")
                Json.decodeFromString<List<Item<Lesson>>>(
                    client.get("/lessons/").bodyAsText()
                ).apply {
                    size shouldBe 2
                }
            }
        }
    }
})
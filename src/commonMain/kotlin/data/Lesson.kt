package data

import common.ItemId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Lesson(
    val name: String,
    val students: Array<GradeInfo> = emptyArray()
) {
    fun addStudent(studentId: StudentId) =
        Lesson(
            name,
            students + GradeInfo(studentId, null)
        )
}

@Serializable
class GradeInfo(
    val studentId: StudentId,
    val grade: Grade?
)

typealias LessonId = ItemId

val Lesson.json
    get(): String = Json.encodeToString(this)


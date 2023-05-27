package ru.popkov.coursework.repo

import common.Item
import data.Lesson
import data.Student
import kotlinx.serialization.json.Json
import org.bson.Document
import org.litote.kmongo.json
import ru.popkov.coursework.AnySerializer
import ru.popkov.coursework.anySerializer

val studentsRepo = MongoRepo<Student>()
val lessonsRepo = MongoRepo<Lesson>()

fun createTestData() {
    listOf(
        Student("Sheldon", "Cooper"),
        Student("Leonard", "Hofstadter"),
        Student("Howard", "Wolowitz"),
        Student("Penny", "Hofstadter"),
    ).apply {
        map {
            studentsRepo.create(it)
        }
    }

//    listOf(
//        Lesson("Math"),
//        Lesson("Phys"),
//        Lesson("Story"),
//    ).apply {
//        map {
//            lessonsRepo.create(it)
//        }
//    }

    val students = studentsRepo.read()
    val studentList = mutableListOf<Item<Student>>()

//    val studentDocument = students.map { Document(it.id, it.elem) }.filter { !it.keys.contains("_id") }
//    val studentDocumentKeys = studentDocument.map { it.keys }
//    val studentDocumentValues = studentDocument.map { it.values }
//
//    studentDocumentValues.forEachIndexed { index, element ->
//        val jsonString = element.first().json
//        val itemSerializer: AnySerializer<Student> = anySerializer()
//        val st = Json.decodeFromString(itemSerializer, jsonString)
//        studentList.add(Item(studentDocumentKeys[index].toString(), st))
//    }
//
//    studentList.forEach {
//        println("efefe student - ${it.id}, ${it.elem.firstname}, ${it.elem.surname}")
//    }

//    val lessons = lessonsRepo.read()
//    val sheldon = students.findLast { it.elem.firstname == "Sheldon" }
//    check(sheldon != null)
//    val leonard = students.findLast { it.elem.firstname == "Leonard" }
//    check(leonard != null)
//    val math = lessons.findLast { it.elem.name == "Math" }
//    check(math != null)
//    val newMath = Lesson(
//        math.elem.name,
//        arrayOf(
//            GradeInfo(sheldon.id, Grade.A),
//            GradeInfo(leonard.id, Grade.B)
//        )
//    )
//    lessonsRepo.update(math.id, newMath)
}

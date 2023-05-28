package ru.popkov.coursework.repo

import com.mongodb.client.MongoDatabase
import common.Item
import data.Grade
import data.GradeInfo
import data.Lesson
import data.Student
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

private val client = KMongo.createClient("mongodb://root:example@127.0.0.1:27017")
private val mongoDatabase: MongoDatabase = client.getDatabase("admin")

private val students = mongoDatabase.getCollection<Student>().apply { drop() }
private val lessons = mongoDatabase.getCollection<Lesson>().apply { drop() }

val studentsRepo = MongoRepo<Student>(students)
val lessonsRepo = MongoRepo<Lesson>(lessons)

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

    listOf(
        Lesson("Math"),
        Lesson("Phys"),
        Lesson("Story"),
    ).apply {
        map {
            lessonsRepo.create(it)
        }
    }

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

    val lessons = lessonsRepo.read()
    val sheldon = students.findLast { it.elem.firstname == "Sheldon" }
    check(sheldon != null)
    val leonard = students.findLast { it.elem.firstname == "Leonard" }
    check(leonard != null)
    val math = lessons.findLast { it.elem.name == "Math" }
    check(math != null)
    val newMath = Lesson(
        math.elem.name,
        arrayOf(
            GradeInfo(sheldon.id, Grade.A),
            GradeInfo(leonard.id, Grade.B)
        )
    )
    lessonsRepo.update(math.id, newMath)
}

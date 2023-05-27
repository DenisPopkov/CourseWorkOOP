package data

import common.ItemId
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
@Serializable
class Student(
    val firstname: String,
    val surname: String
)

typealias StudentId = ItemId

val Student.json
    get(): String = Json.encodeToString(this)


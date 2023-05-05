import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.popkov.transport.timer.server.application.KotlinxGenericMapSerializer
import kotlin.test.assertEquals

@Serializable
data class Box<T>(val contents: T)

@Serializable
data class Data(
    val a: Box<Int> = Box(42),
    val b: Box<Project> = Box(Project("kotlinx.serialization", "Kotlin"))
)

@Serializable
data class Project(val name: String, val language: String)

@Serializable
data class Model2(
    val names: List<String> = listOf("Denis", "Denis2")
)

class MainTest {

    @Test
    fun serializationTest() {
        val jsonString = """{"name": "John", "age": 30}"""
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, jsonString)
        val correctData = listOf<Pair<String, Any>>("java.lang.String" to "John", "java.lang.Long" to 30L)

        model.entries.forEachIndexed { index, value ->
            assertEquals(value.value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value.value, correctData[index].second)  // assert value
        }
    }

    @Test
    fun serializationTest2() {
        val testClass = Data(a = Box(39)).a
        val preModel: String = Json.encodeToString(testClass)
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, preModel)
        val correctData = listOf<Pair<String, Any>>("java.lang.Long" to 39L)

        model.entries.forEachIndexed { index, value ->
            assertEquals(value.value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value.value, correctData[index].second)  // assert value
        }
    }

    @Test
    fun serializationTest3() {
        val testClass = Model2()
        val preModel: String = Json.encodeToString(testClass)
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, preModel)
        val correctData = listOf<Pair<String, Any>>("java.util.ArrayList" to listOf("Denis", "Denis2"))

        model.entries.forEachIndexed { index, value ->
            assertEquals(value.value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value.value, correctData[index].second)  // assert value
        }
    }
}



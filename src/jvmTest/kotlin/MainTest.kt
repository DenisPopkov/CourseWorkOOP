import com.google.gson.Gson
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.popkov.transport.timer.server.application.KotlinxGenericMapSerializer
import ru.popkov.transport.timer.server.application.fromClass
import kotlin.test.assertEquals

data class Test(
    val name: String
)

data class Test2(
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
        val testClass = Test(name = "John").fromClass()
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, testClass)
        val correctData = listOf<Pair<String, Any>>("java.lang.String" to "John")

        model.entries.forEachIndexed { index, value ->
            assertEquals(value.value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value.value, correctData[index].second)  // assert value
        }
    }

    @Test
    fun serializationTest3() {
        val testClass = Test2().fromClass()
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, testClass)
        val correctData = listOf<Pair<String, Any>>("java.util.ArrayList" to listOf("Denis", "Denis2"))

        model.entries.forEachIndexed { index, value ->
            assertEquals(value.value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value.value, correctData[index].second)  // assert value
        }
    }
}



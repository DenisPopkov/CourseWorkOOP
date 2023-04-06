import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.popkov.transport.timer.server.application.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals

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
}
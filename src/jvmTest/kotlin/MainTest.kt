import com.mongodb.client.MongoDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.*
import ru.popkov.transport.timer.server.application.AnyData
import ru.popkov.transport.timer.server.application.KotlinxGenericMapSerializer
import java.time.OffsetDateTime
import kotlin.test.assertEquals

@Serializable
data class Box<T>(val contents: T)

@Serializable
data class Data(
    val a: Box<Int> = Box(42),
    val b: Box<Project> = Box(Project("kotlinx.serialization", "Kotlin"))
)

@Serializable
data class Another(
    val another: Int = 42
)

@Serializable
data class Project(val name: String, val language: String)

class MainTest {

    private lateinit var mongoDatabase: MongoDatabase
    private val jsonString = """{"name": "John", "age": 30}"""

    @Before
    fun init() {
        val client = KMongo.createClient("mongodb://root:example@127.0.0.1:27017")
        mongoDatabase = client.getDatabase("admin")
    }

    // CREATE, READ
    @Test
    fun addToDataBaseTest() {
        // Arrange
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, jsonString)
        val correctData = listOf<Pair<String, Any>>("java.lang.String" to "John", "java.lang.Long" to 30L)
        // Act
        val mDB = mongoDatabase.getCollection<Map<String, Any?>>().apply { drop() }
        mDB.insertOne(model) // Add to MongoDB
        val data = mDB.findOne(jsonString)?.toMutableMap() // Read from MongoDB
        data?.remove("_id")

        // Assert
        data?.values?.forEachIndexed { index, value ->
            assertEquals(value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value, correctData[index].second)  // assert value
        }
    }

    // UPDATE
    @Test
    fun updateDataBaseTest() {
        // Arrange
        val testClass = Another(another = 39)
        val preModel: String = Json.encodeToString(testClass)
        val model: Map<String, Any?> = Json.decodeFromString(KotlinxGenericMapSerializer, preModel)
        val correctData = listOf<Pair<String, Any>>("java.lang.Long" to 50L)

        // Act
        val mDB = mongoDatabase.getCollection<Map<String, Any?>>().apply { drop() }
        val updateData = Another(another = 50)
        mDB.insertOne(model) // Add to MongoDB
        mDB.updateOne(
            and(Another::another eq Another(another = 42).another),
            updateData
        )
        val data = mDB.findOne(Json.encodeToString(updateData))?.toMutableMap() // Read from MongoDB
        data?.remove("_id")

        // Assert
        data?.values?.forEachIndexed { index, value ->
            assertEquals(value?.javaClass?.name, correctData[index].first) // assert Type of value
            assertEquals(value, correctData[index].second)  // assert value
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
}


class AnyClass {
    @Test
    fun test1() {
        val anyData = AnyData(
            name = "hello",
            anyValue = null,
            anyList = listOf("hi", 123, Long.MAX_VALUE, 2.7, true, null),
            anyMap = mapOf(
                true to listOf(123, 25.0, false),
                false to listOf(1.2, 1.3, 0.5),
                "RussianDoll" to mapOf(true to listOf(123, 25.0, false), false to listOf(1.2, 1.3, 0.5))
            ),
            dataFrame = mapOf(
                "date" to listOf(
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(1),
                    OffsetDateTime.now().plusDays(2)
                ),
                "price" to listOf(1.2, 1.3, 0.5)
            ),
        )

        val json = Json.encodeToString(anyData)
        println("json: $json")
        println("class: ${Json.decodeFromString<AnyData>(json)}")
    }
}



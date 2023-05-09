import com.mongodb.client.MongoDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.*
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
data class Another(
    val another: Int = 42
)

@Serializable
data class Project(val name: String, val language: String)

@Serializable
data class Model2(
    val names: List<String> = listOf("Denis", "Denis2")
)

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



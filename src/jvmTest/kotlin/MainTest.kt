import org.junit.Test
import ru.popkov.transport.timer.server.application.ServiceResult
import ru.popkov.transport.timer.server.application.serializeServiceResult
import ru.popkov.transport.timer.server.application.stringToTypeOrError

class MainTest {

    @Test
    fun booleanTest() {
        val serverAnswer = serializeServiceResult(ServiceResult.Success(true))
        val result = stringToTypeOrError<Boolean>(serverAnswer)
        assert(result is Boolean)
    }

    @Test
    fun stringTest() {
        val serverAnswer = serializeServiceResult(ServiceResult.Success("hello"))
        val result = stringToTypeOrError<String>(serverAnswer)
        assert(result is String)
    }

    @Test
    fun intTest() {
        val serverAnswer = serializeServiceResult(ServiceResult.Success(1))
        val result = stringToTypeOrError<Int>(serverAnswer)
        assert(result is Int)
    }
}
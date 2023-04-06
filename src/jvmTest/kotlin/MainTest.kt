import org.junit.Test
import ru.popkov.transport.timer.server.application.ServiceResult
import ru.popkov.transport.timer.server.application.serializeServiceResult
import kotlin.reflect.KClass

class MainTest {

    @Test
    fun booleanTest() {
        val serverAnswer = serializeServiceResult(ServiceResult.Success(true))
        assert(serverAnswer.second is KClass<Boolean>)
    }
}



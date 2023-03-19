import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jonastm.model.*
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    val transportTrips = mutableStateListOf<UserMessageAction>()
    val handler = ServerActionHandlerImpl(transportTrips)
    val chat = ChatStream(handler)

    renderComposable(rootElementId = "root") {
        Div({ style { padding(25.px) } }) {

            H1 { Text(StringsProvider.LOGO) }

            Hr()

            Div(attrs = {
                style {
                    height(100.px)
                    overflow("auto")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.ColumnReverse)
                }
            }) {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                    }
                }) {
                    transportTrips.forEach {
                        val message = it
                        Div(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                alignItems(AlignItems.Baseline)
                                gap(10.px)
                            }
                        }) {
                            P {
                                Text(convertTime(message.time))
                            }
                            P(attrs = {
                                style {
                                    fontWeight(500)
                                }
                            }) {
                                Text("[${it.tripNumber}]: ${it.tripComment}")
                            }
                        }
                    }
                }
            }
        }
    }
}

class ServerActionHandlerImpl(
    private val transportTrips: SnapshotStateList<UserMessageAction>
) : ServerActionHandler {
    override suspend fun onError(e: Exception) {
        transportTrips.add(
            UserMessageAction(0L.toString(), tripNumber = "${e.message}", tripComment = "ERROR")
        )
    }

    override suspend fun onRemoveMessage(action: RemoveAction) {
        var itemToRemove: UserMessageAction? = null
        for (message in transportTrips) {
            if (message.id == action.id) {
                itemToRemove = message
                break
            }
        }
        transportTrips.remove(itemToRemove)
    }

    override suspend fun onUserMessage(action: UserMessageAction) {
        transportTrips.add(action)
    }
}

class ChatStream(
    private val serverActionHandler: ServerActionHandler
) {

    private val client = HttpClient(Js) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
        }
    }

    private var wsSession: DefaultClientWebSocketSession? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            client.webSocket(method = HttpMethod.Get, path = "/ws") {
                wsSession = this
                receive<ServerAction> {
                    onAction(it, serverActionHandler)
                }
            }
        }
    }

    suspend fun send(msg: ClientAction, onError: (Exception) -> Unit) {
        wsSession?.sendMessage(msg, onError) ?: apply {
            onError(Exception("No connected to chat"))
        }
    }
}

fun convertTime(instant: Instant): String {
    val time = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = when {
        (time.hour > 9) -> "${time.hour}"
        else -> " ${time.hour}"
    }
    val minutes = when {
        (time.minute > 9) -> "${time.minute}"
        else -> " ${time.minute}"
    }
    return "$hour:$minutes"
}

suspend inline fun <reified T> DefaultClientWebSocketSession.receive(onMessage: (T) -> Unit) {
    try {
        while (true) {
            val msg = receiveDeserialized<T>()
            onMessage(msg)
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.message)
    }
}

suspend inline fun <reified T> DefaultClientWebSocketSession.sendMessage(msg: T, onError: (Exception) -> Unit) {
    try {
        sendSerialized(msg)
    } catch (e: Exception) {
        println("Error while sending: " + e.message)
        onError(e)
        return
    }
}
